package com.example.sosstaff.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sosstaff.auth.repository.AuthRepository

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    // กำหนดค่าเริ่มต้นให้กับ _loginStatus
    private val _loginStatus = MutableLiveData<AuthRepository.AuthResult>()
    val loginStatus: LiveData<AuthRepository.AuthResult> = _loginStatus

    private val _resetPasswordStatus = MutableLiveData<AuthRepository.AuthResult>()
    val resetPasswordStatus: LiveData<AuthRepository.AuthResult> = _resetPasswordStatus

    fun login(email: String, password: String) {
        _loginStatus.value = AuthRepository.AuthResult.Loading

        // ใช้ LiveData จาก Repository โดยตรง
        val result = repository.login(email, password)
        result.observeForever { authResult ->
            _loginStatus.value = authResult
            // ยกเลิกการ observe เมื่อได้รับผลลัพธ์แล้ว
            result.removeObserver { }
        }
    }

    fun resetPassword(email: String) {
        _resetPasswordStatus.value = AuthRepository.AuthResult.Loading

        // ใช้ LiveData จาก Repository โดยตรง
        val result = repository.resetPassword(email)
        result.observeForever { authResult ->
            _resetPasswordStatus.value = authResult
            // ยกเลิกการ observe เมื่อได้รับผลลัพธ์แล้ว
            result.removeObserver { }
        }
    }
}