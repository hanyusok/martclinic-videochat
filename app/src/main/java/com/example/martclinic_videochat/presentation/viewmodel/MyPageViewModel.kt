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

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.Kakao
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val appointmentRepository: AppointmentRepository,
    private val auth: Auth
) : ViewModel() {

    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: StateFlow<Patient?> = _patient.asStateFlow()

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val sessionStatus: StateFlow<SessionStatus> = auth.sessionStatus

    init {
        viewModelScope.launch {
            auth.sessionStatus.collect { status ->
                if (status is SessionStatus.Authenticated) {
                    loadPatientInfo()
                } else {
                    _patient.value = null
                    _appointments.value = emptyList()
                }
            }
        }
    }

    fun loadPatientInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val activePatient = patientRepository.getFirstPatient()
                _patient.value = activePatient
                if (activePatient?.id != null) {
                    _appointments.value = appointmentRepository.getAppointments(activePatient.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signInWithEmail(emailInput: String, passwordInput: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                auth.signInWith(Email) {
                    email = emailInput
                    password = passwordInput
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "로그인에 실패했습니다.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signUpWithEmail(emailInput: String, passwordInput: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                auth.signUpWith(Email) {
                    email = emailInput
                    password = passwordInput
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "회원가입에 실패했습니다.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signInWithGoogle(onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                auth.signInWith(Google)
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Google 로그인에 실패했습니다.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signInWithKakao(onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                auth.signInWith(Kakao)
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Kakao 로그인에 실패했습니다.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                auth.signOut()
                _patient.value = null
                _appointments.value = emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createPatientProfile(
        nameInput: String,
        phoneInput: String,
        residentLast7Input: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newPatient = Patient(
                    user_id = userId,
                    name = nameInput,
                    phone = phoneInput,
                    resident_last7 = residentLast7Input
                )
                val success = patientRepository.createPatient(newPatient)
                if (success) {
                    loadPatientInfo()
                    onSuccess()
                } else {
                    onError("환자 정보 등록에 실패했습니다.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "환자 정보 등록에 실패했습니다.")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
