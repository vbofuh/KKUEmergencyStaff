// ตัวอย่างไฟล์ viewModelModule.kt
package com.example.sosstaff.common.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

// ประกาศ module สำหรับ ViewModel ทั้งหมด
val viewModelModule = module {
    // ตัวอย่างการลงทะเบียน ViewModel
    // viewModel { YourViewModel(get()) }
}