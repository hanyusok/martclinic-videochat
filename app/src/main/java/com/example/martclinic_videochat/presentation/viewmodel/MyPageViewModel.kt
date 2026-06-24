package com.example.martclinic_videochat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.martclinic_videochat.domain.model.Patient
import com.example.martclinic_videochat.domain.model.UserProfile
import com.example.martclinic_videochat.domain.repository.PatientRepository
import com.example.martclinic_videochat.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.Kakao
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus

import com.example.martclinic_videochat.domain.model.EmrPatient
import com.example.martclinic_videochat.domain.repository.EmrRepository
import com.example.martclinic_videochat.util.PortOneUtil

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val userRepository: UserRepository,
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

    private val _verifiedCustomer = MutableStateFlow<PortOneUtil.VerifiedCustomer?>(null)
    val verifiedCustomer: StateFlow<PortOneUtil.VerifiedCustomer?> = _verifiedCustomer.asStateFlow()

    fun verifyPortOneIdentity(identityVerificationId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val customer = PortOneUtil.getVerifiedCustomer(identityVerificationId)
                if (customer != null) {
                    _verifiedCustomer.value = customer
                    onSuccess()
                } else {
                    onError("본인인증 검증에 실패했습니다. 다시 시도해주세요.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError("인증 처리 중 오류가 발생했습니다.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearVerifiedCustomer() {
        _verifiedCustomer.value = null
    }

    private val _patients = MutableStateFlow<List<Patient>>(emptyList())
    val patients: StateFlow<List<Patient>> = _patients.asStateFlow()

    // Derived state for the account holder's own profile
    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: StateFlow<Patient?> = _patient.asStateFlow()

    val userProfile: StateFlow<UserProfile?> = userRepository.currentUserProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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
                userRepository.getCurrentUserProfile()
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
        skipEmrCheck: Boolean = false,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = auth.currentUserOrNull()?.id ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                var clinicPatientNumber: String? = null
                
                if (!skipEmrCheck) {
                    // Identity Confirmation Step
                    val birthDate = extractBirthDateFromResidentNumber(residentInput)
                    val emrRecord = birthDate?.let { emrRepository.confirmIdentity(nameInput, it) }
                    if (emrRecord != null) {
                        clinicPatientNumber = emrRecord.emr_patient_number?.toString()
                    }
                }

                val newPatient = Patient(
                    user_id = userId,
                    name = nameInput,
                    phone = phoneInput,
                    resident_number = residentInput,
                    relationship = relationshipInput,
                    clinic_patient_number = clinicPatientNumber
                )
                val success = patientRepository.createPatient(newPatient)
                if (success) {
                    // Update user profile completion status if this is the "Self" profile
                    if (relationshipInput == "본인" && nameInput.isNotBlank() && phoneInput.isNotBlank() && residentInput.isNotBlank()) {
                        val currentProfile = userRepository.getCurrentUserProfile()
                        currentProfile?.let { profile ->
                            if (!profile.is_profile_completed) {
                                userRepository.updateUserProfile(profile.copy(is_profile_completed = true))
                            }
                        }
                    }
                    loadPatientInfo()
                    onSuccess()
                } else {
                    onError("환자 정보 등록에 실패했습니다.")
                }
            } catch (e: io.github.jan.supabase.postgrest.exception.PostgrestRestException) {
                e.printStackTrace()
                val message = if (e.code == "23505") {
                    "이미 등록된 전화번호입니다."
                } else {
                    "데이터베이스 오류가 발생했습니다."
                }
                onError(message)
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
                    // Update user profile completion status if this is the "Self" profile
                    if (relationshipInput == "본인" && nameInput.isNotBlank() && phoneInput.isNotBlank() && residentInput.isNotBlank()) {
                        val currentProfile = userRepository.getCurrentUserProfile()
                        currentProfile?.let { profile ->
                            if (!profile.is_profile_completed) {
                                userRepository.updateUserProfile(profile.copy(is_profile_completed = true))
                            }
                        }
                    }
                    loadPatientInfo()
                    onSuccess()
                } else {
                    onError("정보 수정에 실패했습니다.")
                }
            } catch (e: io.github.jan.supabase.postgrest.exception.PostgrestRestException) {
                e.printStackTrace()
                val message = if (e.code == "23505") {
                    "이미 등록된 전화번호입니다."
                } else {
                    "데이터베이스 오류가 발생했습니다."
                }
                onError(message)
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

    fun syncPatientWithEmrDirectly(patient: Patient, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // EMR 연동 시 이름과 주민번호 전체를 사용하여 동명이인 노출 위험을 차단합니다.
                val name = patient.name ?: ""
                val residentNumber = patient.resident_number ?: ""
                val birthDate = extractBirthDateFromResidentNumber(residentNumber)
                val emrRecord = birthDate?.let { emrRepository.confirmIdentity(name, it) }
                if (emrRecord != null) {
                    val updated = patient.copy(
                        name = emrRecord.name ?: patient.name,
                        phone = emrRecord.phone ?: patient.phone,
                        resident_number = emrRecord.resident_number ?: patient.resident_number,
                        clinic_patient_number = emrRecord.emr_patient_number?.toString() ?: patient.clinic_patient_number
                    )
                    patientRepository.updatePatient(updated)
                    loadPatientInfo()
                    onSuccess()
                } else {
                    onError("방문 기록에서 일치하는 환자 정보를 찾을 수 없습니다.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError("방문 기록 연동 중 오류가 발생했습니다.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUserProfile(updatedProfile: UserProfile, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = userRepository.updateUserProfile(updatedProfile)
                if (success) {
                    onSuccess()
                } else {
                    onError("프로필 수정에 실패했습니다.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError("프로필 수정 중 오류가 발생했습니다.")
            } finally {
                _isLoading.value = false
            }
        }
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
