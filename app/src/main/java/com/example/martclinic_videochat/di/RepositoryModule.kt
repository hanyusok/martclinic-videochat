package com.example.martclinic_videochat.di

import com.example.martclinic_videochat.data.repository.AppointmentRepositoryImpl
import com.example.martclinic_videochat.domain.repository.AppointmentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAppointmentRepository(
        appointmentRepositoryImpl: AppointmentRepositoryImpl
    ): AppointmentRepository
}
