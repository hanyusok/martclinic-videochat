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
                    _bookingError.value = "환자의 주민등록번호 형식이 올바르지 않습니다."
                    _bookingSuccess.value = false
                    return@launch
                }

                // 1. Confirm identity in EMR using name and birthdate (YYYY-MM-DD)
                val emrPatient = emrRepository.confirmIdentity(patient.name ?: "", pbirth)
                if (emrPatient == null || emrPatient.emr_patient_number == null) {
                    _bookingError.value = "방문 기록이 없습니다. 접수 직원에게 문의해주세요."
                    _bookingSuccess.value = false
                    return@launch
                }

                val pcode = emrPatient.emr_patient_number

                // 2. Fetch patient detail (Optional verification step)
                val detailSuccess = emrRepository.getPatientDetail(pcode)
                if (!detailSuccess) {
                    _bookingError.value = "병원 환자 정보를 조회하는데 실패했습니다."
                    _bookingSuccess.value = false
                    return@launch
                }

                // 3. Check-in patient to api.calldoctor.co.kr MTR cloud
                val cloudMtrCreate = com.example.martclinic_videochat.data.remote.dto.CloudMtrCreate(
                    pcode = pcode,
                    pname = patient.name,
                    pbirth = pbirth
                )
                val checkInSuccess = emrRepository.checkInPatient(cloudMtrCreate)
                if (!checkInSuccess) {
                    _bookingError.value = "병원 접수 시스템 등록에 실패했습니다."
                    _bookingSuccess.value = false
                    return@launch
                }

                // 4. Save to Supabase
                val appointment = Appointment(
                    patient_id = patientId,
                    status = "waiting", // New status for Queue logic
                    symptoms = symptoms,
                    payment_amount = 10000 // Standard consultation fee
                )
                appointmentRepository.createAppointment(appointment)
                _bookingSuccess.value = true
            } catch (e: Exception) {
                e.printStackTrace()
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
