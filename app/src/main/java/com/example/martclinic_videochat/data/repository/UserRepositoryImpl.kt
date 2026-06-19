package com.example.martclinic_videochat.data.repository

import com.example.martclinic_videochat.domain.model.UserProfile
import com.example.martclinic_videochat.domain.repository.UserRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val auth: Auth
) : UserRepository {

    override suspend fun getUserProfile(userId: String): UserProfile? {
        return try {
            postgrest["profiles"]
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingleOrNull<UserProfile>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun getCurrentUserProfile(): UserProfile? {
        val userId = auth.currentUserOrNull()?.id ?: return null
        return getUserProfile(userId)
    }

    override suspend fun updateUserProfile(userProfile: UserProfile): Boolean {
        return try {
            postgrest["profiles"].update(userProfile) {
                filter { eq("id", userProfile.id) }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
