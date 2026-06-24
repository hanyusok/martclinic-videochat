package com.example.martclinic_videochat.util

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object PortOneUtil {
    const val STORE_ID = "store-4cb29ccf-47c7-4bef-ada9-6c9e79c80166"
    const val CHANNEL_KEY = "channel-key-708b3b04-4a98-4f2d-ab10-e58f909460d5"
    private const val API_SECRET = "QgSunGpLf9PKuDYyUCo9wioZPsQipDh4N65faubf5Z8MzXV4NsyrATL6fsbeXVUVANcBdETSoazOWy5o"
    private const val TAG = "PortOneUtil"

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }

    @Serializable
    data class VerificationResponse(
        val status: String? = null,
        val verifiedCustomer: VerifiedCustomer? = null
    )

    @Serializable
    data class VerifiedCustomer(
        val name: String? = null,
        val phoneNumber: String? = null,
        val birthDate: String? = null,
        val gender: String? = null,
        val ci: String? = null,
        val di: String? = null
    )

    /**
     * Verifies the identity via PortOne V2 API and returns the customer details.
     * @param identityVerificationId The ID returned after successful client-side verification
     * @return VerifiedCustomer details or null if verification failed
     */
    suspend fun getVerifiedCustomer(identityVerificationId: String): VerifiedCustomer? {
        return try {
            val response: HttpResponse = client.get("https://api.portone.io/identity-verifications/$identityVerificationId") {
                header("Authorization", "PortOne $API_SECRET")
            }
            
            val responseText = response.bodyAsText()
            Log.d(TAG, "PortOne verify response: $responseText")
            
            val result = Json { ignoreUnknownKeys = true }.decodeFromString<VerificationResponse>(responseText)
            
            if (result.status == "VERIFIED") {
                result.verifiedCustomer
            } else {
                Log.e(TAG, "Identity verification status is not VERIFIED. Status: ${result.status}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying identity with PortOne", e)
            null
        }
    }
}
