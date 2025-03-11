package com.example.sosstaff.common.di

import com.example.sosstaff.auth.repository.AuthRepository
import com.example.sosstaff.main.chat.repository.ChatRepository
import com.example.sosstaff.main.incidents.repository.IncidentsRepository
import com.example.sosstaff.main.profile.repository.ProfileRepository
import org.koin.dsl.module

// สร้าง Koin module สำหรับระบุการ Dependency Injection
val appModule = module {
    // สร้าง singleton instances สำหรับแต่ละ repository
    single { AuthRepository() }
    single { IncidentsRepository() }
    single { ChatRepository() }
    single { ProfileRepository() }
}