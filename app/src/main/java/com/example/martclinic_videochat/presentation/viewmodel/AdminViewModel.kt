package com.example.martclinic_videochat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.martclinic_videochat.domain.model.AdminStats
import com.example.martclinic_videochat.domain.model.Appointment
import com.example.martclinic_videochat.domain.model.Patient
import com.example.martclinic_videochat.domain.model.Pharmacy
import com.example.martclinic_videochat.domain.model.UserProfile
import com.example.martclinic_videochat.domain.repository.AppointmentRepository
import com.example.martclinic_videochat.domain.repository.PatientRepository
import com.example.martclinic_videochat.domain.repository.PharmacyRepository
import com.example.martclinic_videochat.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.Auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository,
    private val pharmacyRepository: PharmacyRepository,
    private val userRepository: UserRepository,
    private val emrRepository: com.example.martclinic_videochat.domain.repository.EmrRepository,
    private val auth: Auth
) : ViewModel() {

    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    val currentUserProfile: StateFlow<UserProfile?> = _currentUserProfile.asStateFlow()

    private val _allAppointments = MutableStateFlow<List<Appointment>>(emptyList())
    val allAppointments: StateFlow<List<Appointment>> = _allAppointments.asStateFlow()

    private val _allPatients = MutableStateFlow<List<Patient>>(emptyList())
    val allPatients: StateFlow<List<Patient>> = _allPatients.asStateFlow()

    private val _masterPharmacies = MutableStateFlow<List<Pharmacy>>(emptyList())
    val masterPharmacies: StateFlow<List<Pharmacy>> = _masterPharmacies.asStateFlow()

    private val _selectedPatientFavorites = MutableStateFlow<List<Pharmacy>>(emptyList())
    val selectedPatientFavorites: StateFlow<List<Pharmacy>> = _selectedPatientFavorites.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val stats = allAppointments.map { appointments ->
        AdminStats(
            totalPatients = _allPatients.value.size,
            activeAppointments = appointments.count { it.status in Appointment.ACTIVE_STATUSES },
            completedAppointmentsToday = appointments.count { it.status == Appointment.STATUS_COMPLETED },
            totalRevenueToday = appointments.filter { it.status == Appointment.STATUS_COMPLETED }
                .sumOf { it.payment_amount ?: 0 }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminStats())
    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _currentUserProfile.value = userRepository.getCurrentUserProfile()
                _allPatients.value = patientRepository.getAllPatients()
                _allAppointments.value = appointmentRepository.getAllAppointments()
                _masterPharmacies.value = pharmacyRepository.getMasterPharmacies()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            auth.signOut()
        }
    }

    fun updateStatus(appointmentId: String, newStatus: String) {
        viewModelScope.launch {
            appointmentRepository.updateAppointmentStatus(appointmentId, newStatus)
            loadDashboardData()
        }
    }

    fun updateAppointmentDetails(appointmentId: String, status: String, meetLink: String?, paymentAmount: Int?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                appointmentRepository.updateAppointmentDetails(appointmentId, status, meetLink, paymentAmount)
                loadDashboardData()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadPatientFavorites(patientId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _selectedPatientFavorites.value = pharmacyRepository.getFavoritePharmaciesForPatient(patientId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addPatientFavoritePharmacy(patientId: String, pharmacy: Pharmacy) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = pharmacyRepository.addFavoritePharmacy(patientId, pharmacy)
                if (success) {
                    loadPatientFavorites(patientId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removePatientFavoritePharmacy(patientId: String, pharmacyId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = pharmacyRepository.removeFavoritePharmacy(pharmacyId)
                if (success) {
                    loadPatientFavorites(patientId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun togglePatientDefaultPharmacy(patientId: String, pharmacyId: String, isDefault: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = pharmacyRepository.setPharmacyDefault(pharmacyId, patientId, isDefault)
                if (success) {
                    loadPatientFavorites(patientId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePharmacyFax(pharmacyId: String, newFax: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = pharmacyRepository.updatePharmacyFax(pharmacyId, newFax)
                if (success) {
                    loadDashboardData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updatePatientProfile(updatedPatient: Patient) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = patientRepository.updatePatient(updatedPatient)
                if (success) {
                    loadDashboardData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePatientProfile(patientId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = patientRepository.deletePatient(patientId)
                if (success) {
                    loadDashboardData()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchCostForPatient(patient: Patient, onResult: (Int?) -> Unit) {
        viewModelScope.launch {
            val pcode = patient.clinic_patient_number?.toIntOrNull()
            if (pcode == null) {
                onResult(null)
                return@launch
            }
            try {
                val visits = emrRepository.getPatientVisits(pcode)
                val latestVisit = visits.firstOrNull()
                if (latestVisit != null) {
                    val cost = (latestVisit.selfFee ?: 0) + (latestVisit.selfFee2 ?: 0)
                    onResult(if (cost > 0) cost else null)
                } else {
                    onResult(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }
    }
}
