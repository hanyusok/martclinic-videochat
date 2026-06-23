package com.example.martclinic_videochat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.martclinic_videochat.domain.model.Appointment
import com.example.martclinic_videochat.domain.model.Patient
import com.example.martclinic_videochat.domain.repository.EmrRepository
import com.example.martclinic_videochat.domain.repository.AppointmentRepository
import com.example.martclinic_videochat.domain.repository.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val appointmentRepository: AppointmentRepository,
    private val emrRepository: EmrRepository
) : ViewModel() {

    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    val patients: StateFlow<List<Patient>> = _patients.asStateFlow()

    private val _selectedPatient = MutableStateFlow<Patient?>(null)
    val selectedPatient: StateFlow<Patient?> = _selectedPatient.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _bookingSuccess = MutableStateFlow<Boolean?>(null)
    val bookingSuccess: StateFlow<Boolean?> = _bookingSuccess.asStateFlow()

    private val _bookingError = MutableStateFlow<String?>(null)
    val bookingError: StateFlow<String?> = _bookingError.asStateFlow()

    init {
        loadPatients()
    }

    fun loadPatients() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = patientRepository.getPatients()
                _patients.value = list
                if (list.isNotEmpty()) {
                    _selectedPatient.value = list.first { it.relationship == "본인" } ?: list.first()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectPatient(patient: Patient) {
        _selectedPatient.value = patient
    }

    fun requestAsapAppointment(symptoms: String) {
        if (_isLoading.value) return
        val patient = _selectedPatient.value ?: return
        val patientId = patient.id ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _bookingSuccess.value = null
            _bookingError.value = null
            try {
                // Parse birthdate (YYYY-MM-DD) from resident number
                val pbirth = extractBirthDateFromResidentNumber(patient.resident_number)
                if (pbirth == null) {
                    android.util.Log.e("BookingViewModel", "Failed to extract birthdate from resident number: ${patient.resident_number}")
                    _bookingError.value = "환자의 주민등록번호 형식이 올바르지 않습니다."
                    _bookingSuccess.value = false
                    return@launch
                }

                android.util.Log.d("BookingViewModel", "1. Starting identity confirmation in EMR for patient: ${patient.name}, birthdate: $pbirth")
                var pcode: Int? = null
                try {
                    // 1. Confirm identity in EMR using name and birthdate (YYYY-MM-DD)
                    val emrPatient = emrRepository.confirmIdentity(patient.name ?: "", pbirth)
                    if (emrPatient == null || emrPatient.emr_patient_number == null) {
                        android.util.Log.e("BookingViewModel", "EMR Identity confirmation returned null or empty patient number (EMR server non-responding or record missing). Name: ${patient.name}, Birthdate: $pbirth")
                    } else {
                        pcode = emrPatient.emr_patient_number
                        android.util.Log.d("BookingViewModel", "EMR Identity confirmed. EMR Patient Number (pcode): $pcode")

                        // [FIX] Save pcode to Supabase patients table immediately so that
                        // HomeViewModel cost-polling can use clinic_patient_number to query EMR for selfFee.
                        // Without this, clinic_patient_number remains null and the polling loop
                        // short-circuits at: if (pcode != null) — meaning selfFee is NEVER fetched.
                        if (patient.clinic_patient_number != pcode.toString()) {
                            android.util.Log.d("BookingViewModel", "1a. Saving EMR pcode=$pcode to Supabase patients table for patient_id=$patientId")
                            try {
                                val updatedPatient = patient.copy(clinic_patient_number = pcode.toString())
                                patientRepository.updatePatient(updatedPatient)
                                android.util.Log.d("BookingViewModel", "1a. clinic_patient_number saved successfully: pcode=$pcode, patient_id=$patientId")
                            } catch (e: Exception) {
                                android.util.Log.e("BookingViewModel", "1a. FAILED to save clinic_patient_number (pcode=$pcode) to Supabase for patient_id=$patientId — cost polling will not work!", e)
                                // Non-fatal: continue with booking even if this update fails
                            }
                        } else {
                            android.util.Log.d("BookingViewModel", "1a. clinic_patient_number already up-to-date (pcode=$pcode), skipping update.")
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("BookingViewModel", "Exception during EMR identity confirmation for Name: ${patient.name}, Birthdate: $pbirth (EMR server non-responding)", e)
                }

                if (pcode != null) {
                    // 2. Fetch patient detail (Optional verification step)
                    android.util.Log.d("BookingViewModel", "2. Fetching patient detail from EMR for pcode: $pcode")
                    try {
                        val detailSuccess = emrRepository.getPatientDetail(pcode)
                        if (!detailSuccess) {
                            android.util.Log.e("BookingViewModel", "EMR Patient detail fetch failed for pcode: $pcode (EMR server non-responding or record missing).")
                        } else {
                            android.util.Log.d("BookingViewModel", "EMR Patient detail fetched successfully.")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("BookingViewModel", "Exception during EMR patient detail fetch for pcode: $pcode (EMR server non-responding)", e)
                    }

                    // 3. Check-in patient to api.calldoctor.co.kr MTR cloud
                    android.util.Log.d("BookingViewModel", "3. Checking-in patient to EMR MTR cloud for pcode: $pcode")
                    try {
                        val visits = emrRepository.getPatientVisits(pcode)
                        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                        
                        val alreadyCheckedIn = visits.any { visit ->
                            visit.inDate?.startsWith(today) == true &&
                                    visit.fin.isNullOrBlank()
                        }
                        
                        if (alreadyCheckedIn) {
                            android.util.Log.w("BookingViewModel", "Patient pcode $pcode is already checked in today (active session). Skipping duplicate EMR check-in to prevent double registry.")
                        } else {
                            val cloudMtrCreate = com.example.martclinic_videochat.data.remote.dto.CloudMtrCreate(
                                pcode = pcode,
                                pname = patient.name,
                                pbirth = pbirth
                            )
                            val checkInSuccess = emrRepository.checkInPatient(cloudMtrCreate)
                            if (!checkInSuccess) {
                                android.util.Log.e("BookingViewModel", "EMR Check-in failed for pcode: $pcode (EMR server non-responding or registration failed).")
                            } else {
                                android.util.Log.d("BookingViewModel", "EMR Check-in succeeded.")
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("BookingViewModel", "Exception during EMR Check-in or visits validation for pcode: $pcode (EMR server non-responding)", e)
                    }
                } else {
                    android.util.Log.w("BookingViewModel", "Skipping EMR detail fetch and EMR check-in step because patient code (pcode) is null due to earlier EMR server failure or missing record.")
                }

                // 4. Save to Supabase
                android.util.Log.d("BookingViewModel", "4. Saving appointment to Supabase for patient_id: $patientId")
                val appointment = Appointment(
                    patient_id = patientId,
                    status = "payment_pending", // Requires pre-payment before entering queue
                    symptoms = symptoms,
                    payment_amount = null // Cost is fetched from EMR asynchronously
                )
                try {
                    appointmentRepository.createAppointment(appointment)
                    android.util.Log.d("BookingViewModel", "Supabase appointment creation successful.")
                } catch (e: Exception) {
                    android.util.Log.e("BookingViewModel", "Supabase table insert failed (appointments) for patient_id: $patientId", e)
                    throw e
                }
                _bookingSuccess.value = true
            } catch (e: Exception) {
                android.util.Log.e("BookingViewModel", "Error in ASAP appointment flow: ${e.localizedMessage}", e)
                _bookingError.value = "오류가 발생했습니다: ${e.localizedMessage}"
                _bookingSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetBookingStatus() {
        _bookingSuccess.value = null
        _bookingError.value = null
    }

    private fun extractBirthDateFromResidentNumber(residentNumber: String?): String? {
        if (residentNumber == null) return null
        val cleanNumber = residentNumber.replace("-", "")
        if (cleanNumber.length < 7) return null
        
        val yy = cleanNumber.substring(0, 2)
        val mm = cleanNumber.substring(2, 4)
        val dd = cleanNumber.substring(4, 6)
        val genderDigit = cleanNumber.substring(6, 7)
        
        val yearPrefix = if (genderDigit == "1" || genderDigit == "2" || genderDigit == "5" || genderDigit == "6") {
            "19"
        } else if (genderDigit == "3" || genderDigit == "4" || genderDigit == "7" || genderDigit == "8") {
            "20"
        } else {
            "19" // Fallback
        }
        
        return "$yearPrefix$yy-$mm-$dd"
    }
}
