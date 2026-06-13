package com.example.martclinic_videochat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.martclinic_videochat.domain.model.Patient
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

    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    val patients: StateFlow<List<Patient>> = _patients.asStateFlow()

    // Derived state for the account holder's own profile
    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: StateFlow<Patient?> = _patient.asStateFlow()

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
                    _patients.value = emptyList()
                }
            }
        }
    }

    fun loadPatientInfo() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = patientRepository.getPatients()
                _patients.value = list
                _patient.value = list.find { it.relationship == "본인" }
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
                _patients.value = emptyList()
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
        relationshipInput: String = "본인",
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
                    relationship = relationshipInput,
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
        patientId: String,
        nameInput: String,
        phoneInput: String,
        residentInput: String,
        relationshipInput: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val current = _patients.value.find { it.id == patientId } ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updatedPatient = current.copy(
                    name = nameInput,
                    phone = phoneInput,
                    resident_number = residentInput,
                    relationship = relationshipInput
                )
                val success = patientRepository.updatePatient(updatedPatient)
                if (success) {
                    loadPatientInfo()
                    onSuccess()
                } else {
                    onError("정보 수정에 실패했습니다.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError("정보 수정 중 오류가 발생했습니다.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePatientProfile(patientId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = patientRepository.deletePatient(patientId)
                if (success) {
                    loadPatientInfo()
                    onSuccess()
                } else {
                    onError("삭제에 실패했습니다.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError("삭제 중 오류가 발생했습니다.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun syncWithEmrRecord(
        emrPatient: EmrPatient,
        targetPatientId: String?, // Optional: if syncing for a specific family member
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (targetPatientId != null) {
                    val current = _patients.value.find { it.id == targetPatientId } ?: return@launch
                    val updated = current.copy(
                        name = emrPatient.name ?: current.name,
                        phone = emrPatient.phone ?: current.phone,
                        resident_number = emrPatient.resident_number ?: current.resident_number,
                        clinic_patient_number = emrPatient.emr_patient_number?.toString() ?: current.clinic_patient_number
                    )
                    patientRepository.updatePatient(updated)
                } else {
                    // This was likely intended for a new registration or the 'Self' sync
                    // We'll treat it as 'Self' update for simplicity if target is null
                    val self = _patient.value ?: return@launch
                    val updated = self.copy(
                        name = emrPatient.name ?: self.name,
                        phone = emrPatient.phone ?: self.phone,
                        resident_number = emrPatient.resident_number ?: self.resident_number,
                        clinic_patient_number = emrPatient.emr_patient_number?.toString() ?: self.clinic_patient_number
                    )
                    patientRepository.updatePatient(updated)
                }
                loadPatientInfo()
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
                onError("동기화 중 오류가 발생했습니다.")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
