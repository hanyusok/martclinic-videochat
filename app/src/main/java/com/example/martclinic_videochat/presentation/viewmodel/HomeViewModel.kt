package com.example.martclinic_videochat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.martclinic_videochat.domain.model.Appointment
import com.example.martclinic_videochat.domain.model.Patient
import com.example.martclinic_videochat.domain.model.UserRole
import com.example.martclinic_videochat.domain.repository.PatientRepository
import com.example.martclinic_videochat.domain.repository.UserRepository
import com.example.martclinic_videochat.domain.usecase.GetAppointmentsUseCase
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
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val userRepository: UserRepository,
    private val getAppointmentsUseCase: GetAppointmentsUseCase,
    private val auth: Auth
) : ViewModel() {

    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: StateFlow<Patient?> = _patient.asStateFlow()

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _needsProfileUpdate = MutableStateFlow(false)
    val needsProfileUpdate: StateFlow<Boolean> = _needsProfileUpdate.asStateFlow()

    val activeStandby = appointments.map { list ->
        list.find { it.status in Appointment.ACTIVE_STATUSES }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val otherAppointments = appointments.map { list ->
        val standbyId = list.find { it.status in Appointment.ACTIVE_STATUSES }?.id
        if (standbyId != null) list.filter { it.id != standbyId } else list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            auth.sessionStatus.collectLatest { status ->
                if (status is SessionStatus.Authenticated) {
                    loadActivePatientAndAppointments()
                }
            }
        }
    }

    fun loadActivePatientAndAppointments() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Check user role
                val userProfile = userRepository.getCurrentUserProfile()
                _isAdmin.value = userProfile?.role == UserRole.ADMIN
                _needsProfileUpdate.value = userProfile?.is_profile_completed == false

                val activePatient = patientRepository.getFirstPatient()
                _patient.value = activePatient
                if (activePatient?.id != null) {
                    _appointments.value = getAppointmentsUseCase(activePatient.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
