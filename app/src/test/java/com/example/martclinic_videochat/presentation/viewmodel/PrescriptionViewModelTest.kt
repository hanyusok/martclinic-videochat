package com.example.martclinic_videochat.presentation.viewmodel

import com.example.martclinic_videochat.domain.model.Patient
import com.example.martclinic_videochat.domain.model.Pharmacy
import com.example.martclinic_videochat.domain.model.Prescription
import com.example.martclinic_videochat.domain.repository.PatientRepository
import com.example.martclinic_videochat.domain.repository.PharmacyRepository
import com.example.martclinic_videochat.domain.repository.PrescriptionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrescriptionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var patientRepository: FakePatientRepository
    private lateinit var pharmacyRepository: FakePharmacyRepository
    private lateinit var prescriptionRepository: FakePrescriptionRepository
    private lateinit var viewModel: PrescriptionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        patientRepository = FakePatientRepository()
        pharmacyRepository = FakePharmacyRepository()
        prescriptionRepository = FakePrescriptionRepository()
        viewModel = PrescriptionViewModel(patientRepository, pharmacyRepository, prescriptionRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadPrescription_success_setsPrescriptionAndDispatchedPharmacyAndDefaultPharmacy() = runTest(testDispatcher) {
        // Given
        val patient = Patient(id = "patient-1", user_id = "user-1", name = "김철수", phone = "010-1234-5678", resident_last7 = "1234567")
        patientRepository.firstPatient = patient

        val defaultPharmacy = Pharmacy(id = "pharmacy-default", patient_id = "patient-1", pharmacy_name = "사랑약국", address = "서울시 중구", latitude = 37.5, longitude = 126.9, phone = "02-123-4567", is_default = true)
        val otherPharmacy = Pharmacy(id = "pharmacy-sent", patient_id = "patient-1", pharmacy_name = "행복약국", address = "서울시 마포구", latitude = 37.6, longitude = 126.8, phone = "02-987-6543", is_default = false)
        pharmacyRepository.pharmacies.addAll(listOf(defaultPharmacy, otherPharmacy))

        val prescription = Prescription(id = "prescription-1", appointment_id = "appt-1", doctor_notes = "하루 3번 식후 30분", sent_pharmacy_id = "pharmacy-sent", sent_at = "2026-06-12T16:20:32Z")
        prescriptionRepository.prescription = prescription

        // When
        viewModel.loadPrescription("appt-1")
        advanceUntilIdle()

        // Then
        assertEquals(prescription, viewModel.prescription.value)
        assertEquals(otherPharmacy, viewModel.dispatchedPharmacy.value)
        assertEquals(defaultPharmacy, viewModel.defaultPharmacy.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun loadPrescription_noPrescription_setsNull() = runTest(testDispatcher) {
        // Given
        patientRepository.firstPatient = null
        prescriptionRepository.prescription = null

        // When
        viewModel.loadPrescription("appt-nonexistent")
        advanceUntilIdle()

        // Then
        assertNull(viewModel.prescription.value)
        assertNull(viewModel.dispatchedPharmacy.value)
        assertNull(viewModel.defaultPharmacy.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun dispatchPrescriptionToDefaultPharmacy_success_updatesSentStatusAndTriggersSuccess() = runTest(testDispatcher) {
        // Given
        val patient = Patient(id = "patient-1", user_id = "user-1", name = "김철수", phone = "010-1234-5678", resident_last7 = "1234567")
        patientRepository.firstPatient = patient

        val defaultPharmacy = Pharmacy(id = "pharmacy-default", patient_id = "patient-1", pharmacy_name = "사랑약국", address = "서울시 중구", latitude = 37.5, longitude = 126.9, phone = "02-123-4567", is_default = true)
        pharmacyRepository.pharmacies.add(defaultPharmacy)

        val prescription = Prescription(id = "prescription-1", appointment_id = "appt-1", doctor_notes = "하루 3번 식후 30분", sent_pharmacy_id = null, sent_at = null)
        prescriptionRepository.prescription = prescription

        // Load prescription first so ViewModel state has it
        viewModel.loadPrescription("appt-1")
        advanceUntilIdle()

        // When
        viewModel.dispatchPrescriptionToDefaultPharmacy()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.dispatchSuccess.value == true)
        assertEquals(defaultPharmacy, viewModel.dispatchedPharmacy.value)
        assertEquals("pharmacy-default", viewModel.prescription.value?.sent_pharmacy_id)
        assertNotNull(viewModel.prescription.value?.sent_at)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun dispatchPrescriptionToDefaultPharmacy_failure_emitsFailure() = runTest(testDispatcher) {
        // Given
        val patient = Patient(id = "patient-1", user_id = "user-1", name = "김철수", phone = "010-1234-5678", resident_last7 = "1234567")
        patientRepository.firstPatient = patient

        val defaultPharmacy = Pharmacy(id = "pharmacy-default", patient_id = "patient-1", pharmacy_name = "사랑약국", address = "서울시 중구", latitude = 37.5, longitude = 126.9, phone = "02-123-4567", is_default = true)
        pharmacyRepository.pharmacies.add(defaultPharmacy)

        val prescription = Prescription(id = "prescription-1", appointment_id = "appt-1", doctor_notes = "하루 3번 식후 30분", sent_pharmacy_id = null, sent_at = null)
        prescriptionRepository.prescription = prescription
        prescriptionRepository.shouldSendSucceed = false

        viewModel.loadPrescription("appt-1")
        advanceUntilIdle()

        // When
        viewModel.dispatchPrescriptionToDefaultPharmacy()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.dispatchSuccess.value == false)
        assertNull(viewModel.dispatchedPharmacy.value)
        assertNull(viewModel.prescription.value?.sent_pharmacy_id)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun resetDispatchStatus_resetsToNull() = runTest(testDispatcher) {
        // Given
        val patient = Patient(id = "patient-1", user_id = "user-1", name = "김철수", phone = "010-1234-5678", resident_last7 = "1234567")
        patientRepository.firstPatient = patient
        val defaultPharmacy = Pharmacy(id = "pharmacy-default", patient_id = "patient-1", pharmacy_name = "사랑약국", address = "서울시 중구", latitude = 37.5, longitude = 126.9, phone = "02-123-4567", is_default = true)
        pharmacyRepository.pharmacies.add(defaultPharmacy)
        val prescription = Prescription(id = "prescription-1", appointment_id = "appt-1", doctor_notes = "하루 3번 식후 30분", sent_pharmacy_id = null, sent_at = null)
        prescriptionRepository.prescription = prescription

        viewModel.loadPrescription("appt-1")
        advanceUntilIdle()

        viewModel.dispatchPrescriptionToDefaultPharmacy()
        advanceUntilIdle()
        assertTrue(viewModel.dispatchSuccess.value == true)

        // When
        viewModel.resetDispatchStatus()

        // Then
        assertNull(viewModel.dispatchSuccess.value)
    }

    // --- Fakes ---

    class FakePatientRepository : PatientRepository {
        var firstPatient: Patient? = null
        var createPatientResult = true

        override suspend fun getFirstPatient(): Patient? {
            return firstPatient
        }

        override suspend fun createPatient(patient: Patient): Boolean {
            firstPatient = patient
            return createPatientResult
        }
    }

    class FakePharmacyRepository : PharmacyRepository {
        val pharmacies = mutableListOf<Pharmacy>()

        override suspend fun getAllPharmacies(): List<Pharmacy> {
            return pharmacies
        }

        override suspend fun getDefaultPharmacy(patientId: String): Pharmacy? {
            return pharmacies.firstOrNull { it.patient_id == patientId && it.is_default }
        }

        override suspend fun getPharmacyById(id: String): Pharmacy? {
            return pharmacies.firstOrNull { it.id == id }
        }

        override suspend fun setPharmacyDefault(pharmacyId: String, patientId: String, isDefault: Boolean): Boolean {
            return true
        }
    }

    class FakePrescriptionRepository : PrescriptionRepository {
        var prescription: Prescription? = null
        var shouldSendSucceed = true

        override suspend fun getPrescriptionByAppointment(appointmentId: String): Prescription? {
            return prescription
        }

        override suspend fun sendPrescriptionToPharmacy(prescriptionId: String, pharmacyId: String): Boolean {
            return if (shouldSendSucceed) {
                prescription = prescription?.copy(
                    sent_pharmacy_id = pharmacyId,
                    sent_at = "2026-06-12T16:20:32Z"
                )
                true
            } else {
                false
            }
        }
    }
}
