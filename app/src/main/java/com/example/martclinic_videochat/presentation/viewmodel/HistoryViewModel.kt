package com.example.martclinic_videochat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.martclinic_videochat.domain.model.Appointment
import com.example.martclinic_videochat.domain.model.Patient
import com.example.martclinic_videochat.domain.repository.AppointmentRepository
import com.example.martclinic_videochat.domain.repository.PatientRepository
import com.example.martclinic_videochat.domain.repository.PaymentRepository
import com.example.martclinic_videochat.domain.model.Payment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val appointmentRepository: AppointmentRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: StateFlow<Patient?> = _patient.asStateFlow()

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _payments = MutableStateFlow<Map<String, Payment>>(emptyMap())
    val payments: StateFlow<Map<String, Payment>> = _payments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val activePatient = patientRepository.getFirstPatient()
                _patient.value = activePatient
                if (activePatient?.id != null) {
                    val appts = appointmentRepository.getAppointments(activePatient.id)
                    _appointments.value = appts
                    
                    val pMap = mutableMapOf<String, Payment>()
                    for (appt in appts) {
                        if (appt.id != null) {
                            val existingPayments = paymentRepository.getPaymentsForAppointment(appt.id)
                            val successPayment = existingPayments.find { it.status == "SUCCESS" } ?: existingPayments.firstOrNull()
                            if (successPayment != null) {
                                pMap[appt.id] = successPayment
                            }
                        }
                    }
                    _payments.value = pMap
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
