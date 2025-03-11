// พาธ: com.kku.emergencystaff/auth/LoginViewModel.kt
package com.kku.emergencystaff.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kku.emergencystaff.auth.repository.AuthRepository

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    val loginStatus: LiveData<AuthRepository.AuthResult> get() = _loginStatus
    private lateinit var _loginStatus: LiveData<AuthRepository.AuthResult>

    val resetPasswordStatus: LiveData<AuthRepository.AuthResult> get() = _resetPasswordStatus
    private lateinit var _resetPasswordStatus: LiveData<AuthRepository.AuthResult>

    fun login(email: String, password: String) {
        _loginStatus = repository.login(email, password)
    }

    fun resetPassword(email: String) {
        _resetPasswordStatus = repository.resetPassword(email)
    }
}

// Factory pattern สำหรับการสร้าง ViewModel พร้อมกับ Repository
class LoginViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}