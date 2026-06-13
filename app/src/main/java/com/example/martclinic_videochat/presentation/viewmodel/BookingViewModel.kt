package com.example.martclinic_videochat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.martclinic_videochat.domain.model.Appointment
import com.example.martclinic_videochat.domain.model.Patient
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
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    val patients: StateFlow<List<Patient>> = _patients.asStateFlow()

    private val _selectedPatient = MutableStateFlow<Patient?>(null)
    val selectedPatient: StateFlow<Patient?> = _selectedPatient.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _bookingSuccess = MutableStateFlow<Boolean?>(null)
    val bookingSuccess: StateFlow<Boolean?> = _bookingSuccess.asStateFlow()

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
        val patientId = _selectedPatient.value?.id ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _bookingSuccess.value = null
            try {
                // In ASAP mode, we join the waiting list directly
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
                _bookingSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetBookingStatus() {
        _bookingSuccess.value = null
    }
}
