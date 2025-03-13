// ตัวอย่างไฟล์ viewModelModule.kt
package com.example.sosstaff.common.di

import com.example.sosstaff.auth.LoginViewModel
import com.example.sosstaff.main.chat.viewmodels.ChatViewModel
import com.example.sosstaff.main.incidents.viewmodels.IncidentsViewModel
import com.example.sosstaff.main.profile.viewmodels.ProfileViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

// ประกาศ module สำหรับ ViewModel ทั้งหมด
val viewModelModule = module {
    // ลงทะเบียน ViewModel ต่างๆ
    viewModel { LoginViewModel(get()) }
    viewModel { ChatViewModel(get()) }
    viewModel { IncidentsViewModel(get()) }
    viewModel { ProfileViewModel(get()) }
}