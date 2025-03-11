package com.example.sosstaff.common.di

import com.example.sosstaff.auth.repository.AuthRepository
import com.example.sosstaff.main.chat.repository.ChatRepository
import com.example.sosstaff.main.incidents.repository.IncidentsRepository
import com.example.sosstaff.main.profile.repository.ProfileRepository
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