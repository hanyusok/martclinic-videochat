package com.example.martclinic_videochat.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.martclinic_videochat.domain.model.Appointment
import com.example.martclinic_videochat.domain.model.Patient
import com.example.martclinic_videochat.domain.model.Schedule
import com.example.martclinic_videochat.domain.repository.AppointmentRepository
import com.example.martclinic_videochat.domain.repository.PatientRepository
import com.example.martclinic_videochat.domain.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val scheduleRepository: ScheduleRepository,
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    private val _patient = MutableStateFlow<Patient?>(null)
    val patient: StateFlow<Patient?> = _patient.asStateFlow()

    private val _availableDates = MutableStateFlow<List<String>>(emptyList())
    val availableDates: StateFlow<List<String>> = _availableDates.asStateFlow()

    private val _selectedDate = MutableStateFlow<String?>(null)
    val selectedDate: StateFlow<String?> = _selectedDate.asStateFlow()

    private val _schedules = MutableStateFlow<List<Schedule>>(emptyList())
    val schedules: StateFlow<List<Schedule>> = _schedules.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _bookingSuccess = MutableStateFlow<Boolean?>(null)
    val bookingSuccess: StateFlow<Boolean?> = _bookingSuccess.asStateFlow()

    init {
        loadPatientAndDates()
    }

    fun loadPatientAndDates() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val activePatient = patientRepository.getFirstPatient()
                _patient.value = activePatient
                
                val dates = scheduleRepository.getAvailableDates()
                _availableDates.value = dates
                if (dates.isNotEmpty()) {
                    selectDate(dates.first())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _schedules.value = scheduleRepository.getSchedulesByDate(date)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun bookAppointment(schedule: Schedule, symptoms: String) {
        val patientId = _patient.value?.id ?: return
        val scheduleId = schedule.id ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _bookingSuccess.value = null
            try {
                // 1. Mark schedule as booked
                val isBooked = scheduleRepository.bookSchedule(scheduleId, patientId)
                if (isBooked) {
                    // 2. Create appointment record
                    val randomCode = (100..999).random()
                    val meetLink = "https://meet.google.com/abc-defg-$randomCode"
                    val appointment = Appointment(
                        patient_id = patientId,
                        schedule_id = scheduleId,
                        status = "confirmed",
                        symptoms = symptoms,
                        meet_link = meetLink,
                        payment_amount = 10000
                    )
                    appointmentRepository.createAppointment(appointment)
                    _bookingSuccess.value = true
                    
                    // Refresh schedules
                    _selectedDate.value?.let { selectDate(it) }
                } else {
                    _bookingSuccess.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _bookingSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetBookingStatus() {
        _bookingSuccess.value = null
    }
}
