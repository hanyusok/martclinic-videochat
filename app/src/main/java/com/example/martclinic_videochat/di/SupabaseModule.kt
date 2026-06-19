package com.example.martclinic_videochat.di

import com.example.martclinic_videochat.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.ktor.client.plugins.HttpTimeout
import javax.inject.Singleton
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.composeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import kotlin.time.Duration.Companion.seconds

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @OptIn(io.github.jan.supabase.annotations.SupabaseInternal::class)
    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        val createClient: (Boolean) -> SupabaseClient = { autoLoad ->
            createSupabaseClient(
                supabaseUrl = Constants.SUPABASE_URL,
                supabaseKey = Constants.SUPABASE_ANON_KEY
            ) {
                httpConfig {
                    install(HttpTimeout) {
                        requestTimeoutMillis = 30.seconds.inWholeMilliseconds
                        connectTimeoutMillis = 30.seconds.inWholeMilliseconds
                        socketTimeoutMillis = 30.seconds.inWholeMilliseconds
                    }
                }
                install(Postgrest)
                install(Auth) {
                    autoLoadFromStorage = autoLoad
                    autoSaveToStorage = true
                }
                install(ComposeAuth) {
                    googleNativeLogin(serverClientId = "8h4knsue8ei77hjhcluppsm005pud4jb.apps.googleusercontent.com")
                }
                install(Storage)
                install(Functions)
                install(Realtime)
            }
        }

        return try {
            createClient(true)
        } catch (e: Exception) {
            // If session loading fails (e.g. due to corrupted storage), fallback to no auto-load
            createClient(false)
        }
    }

    @Provides
    @Singleton
    fun provideSupabaseDatabase(client: SupabaseClient): Postgrest {
        return client.postgrest
    }

    @Provides
    @Singleton
    fun provideSupabaseAuth(client: SupabaseClient): Auth {
        return client.auth
    }

    @Provides
    @Singleton
    fun provideSupabaseStorage(client: SupabaseClient): Storage {
        return client.storage
    }

    @Provides
    @Singleton
    fun provideSupabaseFunctions(client: SupabaseClient): Functions {
        return client.functions
    }

    @Provides
    @Singleton
    fun provideSupabaseRealtime(client: SupabaseClient): Realtime {
        return client.realtime
    }
}
