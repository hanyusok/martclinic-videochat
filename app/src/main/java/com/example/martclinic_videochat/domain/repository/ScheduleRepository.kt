package com.example.martclinic_videochat.domain.repository

import com.example.martclinic_videochat.domain.model.Schedule

interface ScheduleRepository {
    suspend fun getSchedulesByDate(date: String): List<Schedule>
    suspend fun getAvailableDates(): List<String>
    suspend fun bookSchedule(scheduleId: String, patientId: String): Boolean
}
