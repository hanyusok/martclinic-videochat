package com.example.martclinic_videochat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.martclinic_videochat.domain.model.Appointment
import com.example.martclinic_videochat.domain.model.Patient
import com.example.martclinic_videochat.domain.model.UserRole
import com.example.martclinic_videochat.domain.repository.PatientRepository
import com.example.martclinic_videochat.domain.repository.UserRepository
import com.example.martclinic_videochat.domain.repository.AppointmentRepository
import com.example.martclinic_videochat.domain.repository.EmrRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val userRepository: UserRepository,
    private val appointmentRepository: AppointmentRepository,
    private val emrRepository: EmrRepository,
    private val auth: Auth
) : ViewModel() {

    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: StateFlow<Patient?> = _patient.asStateFlow()

    private val _allPatients = MutableStateFlow<List<Patient>>(emptyList())
    val allPatients: StateFlow<List<Patient>> = _allPatients.asStateFlow()

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val isAdmin: StateFlow<Boolean> = userRepository.currentUserProfile
        .map { it?.role == UserRole.ADMIN }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val needsProfileUpdate: StateFlow<Boolean> = kotlinx.coroutines.flow.combine(
        patient,
        isLoading
    ) { currentPatient, loading ->
        if (loading && currentPatient == null) {
            false
        } else {
            currentPatient == null || 
            currentPatient.name.isNullOrBlank() || 
            currentPatient.phone.isNullOrBlank() || 
            currentPatient.resident_number.isNullOrBlank()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val activeStandby = appointments.map { list ->
        list.find { it.status in Appointment.ACTIVE_STATUSES }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val otherAppointments = appointments.map { list ->
        val standbyId = list.find { it.status in Appointment.ACTIVE_STATUSES }?.id
        if (standbyId != null) list.filter { it.id != standbyId } else list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var pollingJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            auth.sessionStatus.collectLatest { status ->
                if (status is SessionStatus.Authenticated) {
                    loadActivePatientAndAppointments()
                    startCostPolling()
                } else {
                    pollingJob?.cancel()
                }
            }
        }
    }

    private fun startCostPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            var pollingDelay = 5000L
            while(true) {
                val standby = activeStandby.value
                // If patient is waiting for cost (payment_amount is null)
                if (standby != null && standby.status == "payment_pending" && standby.payment_amount == null) {
                    val patient = allPatients.value.find { it.id == standby.patient_id }
                    if (patient != null) {
                        val pcode = patient.clinic_patient_number?.toIntOrNull()
                        if (pcode != null) {
                            try {
                                val cost = emrRepository.getTodayConsultationCost(pcode)
                                pollingDelay = 5000L // Reset delay on success
                                if (cost != null) {
                                    Log.d(TAG, "[CostPolling] EMR cost found=$cost for pcode=$pcode, appointment=${standby.id}")
                                    appointmentRepository.updateAppointmentPaymentAmount(standby.id!!, cost)
                                    loadActivePatientAndAppointments()
                                } else {
                                    Log.d(TAG, "[CostPolling] EMR cost not yet available for pcode=$pcode")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "[CostPolling] EMR getTodayConsultationCost FAILED for pcode=$pcode (EMR server non-responding)", e)
                                e.printStackTrace()
                                // Double the delay on exception (exponential backoff) up to a max of 60 seconds
                                pollingDelay = (pollingDelay * 2).coerceAtMost(60000L)
                            }
                        }
                    }
                }
                delay(pollingDelay)
            }
        }
    }

    fun loadActivePatientAndAppointments() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Ensure profile is loaded for reactive streams
                val userProfile = userRepository.getCurrentUserProfile()

                val allPatientsList = patientRepository.getPatients()
                _allPatients.value = allPatientsList
                val activePatient = allPatientsList.firstOrNull { it.relationship == "본인" } ?: allPatientsList.firstOrNull()
                _patient.value = activePatient
                
                val patientIds = allPatientsList.mapNotNull { it.id }
                if (patientIds.isNotEmpty()) {
                    _appointments.value = appointmentRepository.getAppointmentsForPatients(patientIds)
                } else {
                    _appointments.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "[HomeViewModel] loadActivePatientAndAppointments FAILED", e)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun processPayment(appointmentId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d(TAG, "[HomeViewModel] processPayment: appointmentId=$appointmentId -> status=${Appointment.STATUS_WAITING}")
                appointmentRepository.updateAppointmentStatus(appointmentId, Appointment.STATUS_WAITING)
                Log.d(TAG, "[HomeViewModel] processPayment success")
                loadActivePatientAndAppointments()
            } catch (e: Exception) {
                Log.e(TAG, "[HomeViewModel] processPayment FAILED for appointmentId=$appointmentId", e)
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}
