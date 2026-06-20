package com.example.martclinic_videochat.data.repository

import com.example.martclinic_videochat.domain.model.UserProfile
import com.example.martclinic_videochat.domain.repository.UserRepository
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val auth: Auth
) : UserRepository {

    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    override val currentUserProfile: Flow<UserProfile?> = _currentUserProfile.asStateFlow()

    init {
        // Automatically clear profile on logout and fetch on login
        CoroutineScope(Dispatchers.IO).launch {
            auth.sessionStatus.collect { status ->
                if (status is SessionStatus.Authenticated) {
                    getCurrentUserProfile()
                } else if (status is SessionStatus.NotAuthenticated) {
                    _currentUserProfile.value = null
                }
            }
        }
    }

    override suspend fun getUserProfile(userId: String): UserProfile? {
        return try {
            val profile = postgrest["profiles"]
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingleOrNull<UserProfile>()
            
            // Only update the stream if this is the logged-in user
            if (userId == auth.currentUserOrNull()?.id) {
                _currentUserProfile.value = profile
            }
            profile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun getCurrentUserProfile(): UserProfile? {
        val userId = auth.currentUserOrNull()?.id ?: return null
        return getUserProfile(userId)
    }

    override suspend fun refreshCurrentUserProfile() {
        getCurrentUserProfile()
    }

    override suspend fun updateUserProfile(userProfile: UserProfile): Boolean {
        return try {
            postgrest["profiles"].update(userProfile) {
                filter { eq("id", userProfile.id) }
            }
            // Update local state immediately for snappy UI
            if (userProfile.id == auth.currentUserOrNull()?.id) {
                _currentUserProfile.value = userProfile
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
