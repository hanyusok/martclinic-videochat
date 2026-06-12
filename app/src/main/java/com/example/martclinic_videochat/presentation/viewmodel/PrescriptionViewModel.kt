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

    // Add this to track the specific pharmacy the prescription was sent to
    private val _dispatchedPharmacy = MutableStateFlow<Pharmacy?>(null)
    val dispatchedPharmacy: StateFlow<Pharmacy?> = _dispatchedPharmacy.asStateFlow()
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

                // 2. Fetch the pharmacy it was sent to (if any)
                if (record?.sent_pharmacy_id != null) {
                    _dispatchedPharmacy.value = pharmacyRepository.getPharmacyById(record.sent_pharmacy_id)
                } else {
                    _dispatchedPharmacy.value = null
                }

                // 3. Fetch current default pharmacy for potential new dispatch
                val activePatient = patientRepository.getFirstPatient()
                val patientId = activePatient?.id
                if (patientId != null) {
                    _defaultPharmacy.value = pharmacyRepository.getDefaultPharmacy(patientId)
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
                    // Update dispatched pharmacy info immediately
                    _dispatchedPharmacy.value = pharmacy
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
