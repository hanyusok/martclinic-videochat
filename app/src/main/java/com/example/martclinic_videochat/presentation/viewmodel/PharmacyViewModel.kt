package com.example.martclinic_videochat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.martclinic_videochat.data.location.LocationHelper
import com.example.martclinic_videochat.domain.model.Patient
import com.example.martclinic_videochat.domain.model.Pharmacy
import com.example.martclinic_videochat.domain.repository.PatientRepository
import com.example.martclinic_videochat.domain.repository.PharmacyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PharmacyViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val pharmacyRepository: PharmacyRepository,
    private val locationHelper: LocationHelper
) : ViewModel() {

    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: StateFlow<Patient?> = _patient.asStateFlow()

    private val _pharmacies = MutableStateFlow<List<Pharmacy>>(emptyList())
    val pharmacies: StateFlow<List<Pharmacy>> = _pharmacies.asStateFlow()

    private val _nearbyPharmacies = MutableStateFlow<List<Pharmacy>>(emptyList())
    val nearbyPharmacies: StateFlow<List<Pharmacy>> = _nearbyPharmacies.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadPatientAndPharmacies()
    }

    fun loadPatientAndPharmacies() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val activePatient = patientRepository.getFirstPatient()
                _patient.value = activePatient
                
                // Get real device location
                val location = locationHelper.getCurrentLocation()
                val lat = location?.latitude ?: 37.5665 // Fallback to Seoul City Hall
                val lon = location?.longitude ?: 126.9780
                
                // Fetch and sync nearby pharmacies from external API
                val syncResult = pharmacyRepository.fetchAndStoreNearbyPharmacies(lat, lon)
                
                // Fetch from our local master list after sync (10km radius)
                _nearbyPharmacies.value = pharmacyRepository.getNearbyPharmacies(lat, lon, 10000.0)

                _pharmacies.value = pharmacyRepository.getAllPharmacies()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleDefaultPharmacy(pharmacy: Pharmacy, isDefault: Boolean) {
        val patientId = _patient.value?.id ?: return
        val pharmacyId = pharmacy.id ?: return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = pharmacyRepository.setPharmacyDefault(pharmacyId, patientId, isDefault)
                if (success) {
                    _pharmacies.value = pharmacyRepository.getAllPharmacies()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addFavoritePharmacy(pharmacy: Pharmacy) {
        val patientId = _patient.value?.id ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = pharmacyRepository.addFavoritePharmacy(patientId, pharmacy)
                if (success) {
                    _pharmacies.value = pharmacyRepository.getAllPharmacies()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
