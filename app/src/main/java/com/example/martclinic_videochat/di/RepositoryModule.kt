package com.example.martclinic_videochat.di

import com.example.martclinic_videochat.data.repository.AppointmentRepositoryImpl
import com.example.martclinic_videochat.data.repository.PatientRepositoryImpl
import com.example.martclinic_videochat.data.repository.PharmacyRepositoryImpl
import com.example.martclinic_videochat.data.repository.PrescriptionRepositoryImpl
import com.example.martclinic_videochat.data.repository.UserRepositoryImpl
import com.example.martclinic_videochat.domain.repository.AppointmentRepository
import com.example.martclinic_videochat.domain.repository.PatientRepository
import com.example.martclinic_videochat.domain.repository.PharmacyRepository
import com.example.martclinic_videochat.domain.repository.PrescriptionRepository
import com.example.martclinic_videochat.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import com.example.martclinic_videochat.data.repository.EmrRepositoryImpl
import com.example.martclinic_videochat.domain.repository.EmrRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindEmrRepository(
        emrRepositoryImpl: EmrRepositoryImpl
    ): EmrRepository

    @Binds
    @Singleton
    abstract fun bindAppointmentRepository(
        appointmentRepositoryImpl: AppointmentRepositoryImpl
    ): AppointmentRepository

    @Binds
    @Singleton
    abstract fun bindPatientRepository(
        patientRepositoryImpl: PatientRepositoryImpl
    ): PatientRepository

    @Binds
    @Singleton
    abstract fun bindPharmacyRepository(
        pharmacyRepositoryImpl: PharmacyRepositoryImpl
    ): PharmacyRepository

    @Binds
    @Singleton
    abstract fun bindPrescriptionRepository(
        prescriptionRepositoryImpl: PrescriptionRepositoryImpl
    ): PrescriptionRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository
}
