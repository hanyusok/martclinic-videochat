package com.example.martclinic_videochat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.martclinic_videochat.domain.model.Appointment
import com.example.martclinic_videochat.domain.model.Patient
import com.example.martclinic_videochat.domain.usecase.GetAppointmentsUseCase
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

import com.example.martclinic_videochat.domain.model.EmrPatient
import com.example.martclinic_videochat.domain.repository.EmrRepository

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val emrRepository: EmrRepository,
    private val getAppointmentsUseCase: GetAppointmentsUseCase,
    private val auth: Auth
) : ViewModel() {

    private val _emrSearchResults = MutableStateFlow<List<EmrPatient>>(emptyList())
    val emrSearchResults: StateFlow<List<EmrPatient>> = _emrSearchResults.asStateFlow()

    private val _isEmrLoading = MutableStateFlow(false)
    val isEmrLoading: StateFlow<Boolean> = _isEmrLoading.asStateFlow()

    fun searchEmrPatients(name: String) {
        viewModelScope.launch {
            _isEmrLoading.value = true
            try {
                _emrSearchResults.value = emrRepository.searchPatientsByName(name)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isEmrLoading.value = false
            }
        }
    }

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
                    _appointments.value = getAppointmentsUseCase(activePatient.id)
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
                auth.signInWith(Google, redirectUrl = "martclinic://login-callback")
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
                auth.signInWith(Kakao, redirectUrl = "martclinic://login-callback")
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
        residentInput: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Identity Confirmation Step
                val emrRecord = emrRepository.confirmIdentity(nameInput, residentInput)
                if (emrRecord == null) {
                    onError("병원 EMR 데이터에서 환자 정보를 확인할 수 없습니다. 정보를 다시 확인해 주세요.")
                    _isLoading.value = false
                    return@launch
                }

                val newPatient = Patient(
                    user_id = userId,
                    name = nameInput,
                    phone = phoneInput,
                    resident_number = residentInput,
                    clinic_patient_number = emrRecord.emr_patient_number?.toString()
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

    fun updatePatientProfile(
        nameInput: String,
        phoneInput: String,
        residentInput: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentPatient = _patient.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Simplified Update: No mandatory EMR check for manual edits
                val updatedPatient = currentPatient.copy(
                    name = nameInput,
                    phone = phoneInput,
                    resident_number = residentInput
                )
                val success = patientRepository.updatePatient(updatedPatient)
                if (success) {
                    loadPatientInfo()
                    onSuccess()
                } else {
                    onError("환자 정보 수정에 실패했습니다.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "환자 정보 수정에 실패했습니다.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Dedicated method for EMR Sync (Search -> Pick -> Sync)
     */
    fun syncWithEmrRecord(
        emrPatient: EmrPatient,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentPatient = _patient.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updatedPatient = currentPatient.copy(
                    name = emrPatient.name ?: currentPatient.name,
                    phone = emrPatient.phone ?: currentPatient.phone,
                    resident_number = emrPatient.resident_number ?: currentPatient.resident_number,
                    clinic_patient_number = emrPatient.emr_patient_number?.toString() ?: currentPatient.clinic_patient_number
                )
                val success = patientRepository.updatePatient(updatedPatient)
                if (success) {
                    loadPatientInfo()
                    onSuccess()
                } else {
                    onError("EMR 정보 동기화에 실패했습니다.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError("동기화 중 오류가 발생했습니다.")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
