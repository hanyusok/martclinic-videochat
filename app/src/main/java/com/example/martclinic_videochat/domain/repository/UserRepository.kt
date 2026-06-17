package com.example.martclinic_videochat.domain.repository

import com.example.martclinic_videochat.domain.model.UserProfile

interface UserRepository {
    suspend fun getUserProfile(userId: String): UserProfile?
    suspend fun getCurrentUserProfile(): UserProfile?
}
