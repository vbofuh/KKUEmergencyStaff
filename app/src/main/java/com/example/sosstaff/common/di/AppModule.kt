// พาธ: com.kku.emergencystaff/common/di/AppModule.kt
package com.kku.emergencystaff.common.di

import com.kku.emergencystaff.auth.repository.AuthRepository
import com.kku.emergencystaff.main.chat.repository.ChatRepository
import com.kku.emergencystaff.main.incidents.repository.IncidentsRepository
import com.kku.emergencystaff.main.profile.repository.ProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
        return AuthRepository()
    }

    @Provides
    @Singleton
    fun provideIncidentsRepository(): IncidentsRepository {
        return IncidentsRepository()
    }

    @Provides
    @Singleton
    fun provideChatRepository(): ChatRepository {
        return ChatRepository()
    }

    @Provides
    @Singleton
    fun provideProfileRepository(): ProfileRepository {
        return ProfileRepository()
    }
}