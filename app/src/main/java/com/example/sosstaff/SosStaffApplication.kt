package com.example.sosstaff

import android.app.Application
import com.example.sosstaff.common.di.appModule
import com.example.sosstaff.common.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SosStaffApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // เริ่มต้น Koin
        startKoin {
            androidContext(this@SosStaffApplication)
            modules(listOf(appModule, viewModelModule))
        }
    }
}