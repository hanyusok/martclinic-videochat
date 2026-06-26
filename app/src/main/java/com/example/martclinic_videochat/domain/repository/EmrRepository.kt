package com.example.martclinic_videochat.domain.repository

import com.example.martclinic_videochat.domain.model.EmrPatient

interface EmrRepository {
    suspend fun searchPatientsByName(name: String): List<EmrPatient>
    suspend fun confirmIdentity(name: String, birthDate: String): EmrPatient?
    suspend fun checkInPatient(cloudMtrCreate: com.example.martclinic_videochat.data.remote.dto.CloudMtrCreate): Boolean
    suspend fun getPatientVisits(pcode: Int): List<com.example.martclinic_videochat.domain.model.EmrVisit>
    suspend fun getTodayConsultationCost(pcode: Int): Int?
    suspend fun createChart(chartCreate: com.example.martclinic_videochat.data.remote.dto.CloudChartCreate): Boolean
}
