package com.example.martclinic_videochat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.martclinic_videochat.domain.model.Pharmacy
import com.example.martclinic_videochat.domain.model.Prescription
import com.example.martclinic_videochat.domain.repository.PatientRepository
import com.example.martclinic_videochat.domain.repository.PharmacyRepository
import com.example.martclinic_videochat.domain.repository.PrescriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrescriptionViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val pharmacyRepository: PharmacyRepository,
    private val prescriptionRepository: PrescriptionRepository
) : ViewModel() {

    private val _prescription = MutableStateFlow<Prescription?>(null)
    val prescription: StateFlow<Prescription?> = _prescription.asStateFlow()

    private val _defaultPharmacy = MutableStateFlow<Pharmacy?>(null)
    val defaultPharmacy: StateFlow<Pharmacy?> = _defaultPharmacy.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _dispatchSuccess = MutableStateFlow<Boolean?>(null)
    val dispatchSuccess: StateFlow<Boolean?> = _dispatchSuccess.asStateFlow()

    fun loadPrescription(appointmentId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _dispatchSuccess.value = null
            try {
                // 1. Fetch prescription
                val record = prescriptionRepository.getPrescriptionByAppointment(appointmentId)
                _prescription.value = record
                
                // 2. Fetch patient's default pharmacy
                val activePatient = patientRepository.getFirstPatient()
                if (activePatient != null) {
                    val list = pharmacyRepository.getAllPharmacies()
                    val defaultPharm = list.firstOrNull { it.is_default && it.patient_id == activePatient.id }
                    _defaultPharmacy.value = defaultPharm
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun dispatchPrescriptionToDefaultPharmacy() {
        val pres = _prescription.value ?: return
        val pharmacy = _defaultPharmacy.value ?: return
        val presId = pres.id ?: return
        val pharmacyId = pharmacy.id ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _dispatchSuccess.value = null
            try {
                val success = prescriptionRepository.sendPrescriptionToPharmacy(presId, pharmacyId)
                if (success) {
                    _dispatchSuccess.value = true
                    // Reload prescription to update sent status
                    _prescription.value = prescriptionRepository.getPrescriptionByAppointment(pres.appointment_id)
                } else {
                    _dispatchSuccess.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _dispatchSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun resetDispatchStatus() {
        _dispatchSuccess.value = null
    }
}
