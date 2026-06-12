package com.example.martclinic_videochat.data.repository

import com.example.martclinic_videochat.domain.model.Schedule
import com.example.martclinic_videochat.domain.repository.ScheduleRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest
) : ScheduleRepository {

    override suspend fun getSchedulesByDate(date: String): List<Schedule> {
        return try {
            postgrest["schedules"]
                .select {
                    filter {
                        eq("date", date)
                    }
                    order("start_time", Order.ASCENDING)
                }
                .decodeList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun getAvailableDates(): List<String> {
        return try {
            val list = postgrest["schedules"]
                .select()
                .decodeList<Schedule>()
            list.map { it.date }.distinct().sorted()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun bookSchedule(scheduleId: String, patientId: String): Boolean {
        return try {
            postgrest["schedules"].update(
                mapOf(
                    "is_available" to false,
                    "booked_by" to patientId
                )
            ) {
                filter {
                    eq("id", scheduleId)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
