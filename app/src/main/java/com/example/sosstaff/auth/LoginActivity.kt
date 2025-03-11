// พาธ: com.kku.emergencystaff/auth/LoginActivity.kt
package com.kku.emergencystaff.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.sosstaff.R
import com.kku.emergencystaff.auth.repository.AuthRepository
import com.kku.emergencystaff.main.MainContainer

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var forgotPasswordButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // ผูกวิวกับตัวแปร
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton)
        progressBar = findViewById(R.id.progressBar)

        // ตั้งค่า ViewModel
        val repository = AuthRepository()
        val factory = LoginViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]

        // ตั้งค่าการสังเกตการณ์ LiveData
        observeViewModel()

        // ตั้งค่าตัวจัดการเหตุการณ์คลิกปุ่ม
        setupClickListeners()
    }

    private fun observeViewModel() {
        viewModel.loginStatus.observe(this) { result ->
            when (result) {
                is AuthRepository.AuthResult.Loading -> showLoading()
                is AuthRepository.AuthResult.Success -> {
                    hideLoading()
                    navigateToMainContainer()
                }
                is AuthRepository.AuthResult.Error -> {
                    hideLoading()
                    showError(result.message)
                }
            }
        }

        viewModel.resetPasswordStatus.observe(this) { result ->
            when (result) {
                is AuthRepository.AuthResult.Loading -> showLoading()
                is AuthRepository.AuthResult.Success -> {
                    hideLoading()
                    Toast.makeText(this, "ส่งลิงก์รีเซ็ตรหัสผ่านไปยังอีเมลของคุณแล้ว", Toast.LENGTH_LONG).show()
                }
                is AuthRepository.AuthResult.Error -> {
                    hideLoading()
                    showError(result.message)
                }
            }
        }
    }

    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (validateInputs(email, password)) {
                viewModel.login(email, password)
            }
        }

        forgotPasswordButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isNotEmpty()) {
                viewModel.resetPassword(email)
            } else {
                emailEditText.error = "กรุณากรอกอีเมล"
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            emailEditText.error = "กรุณากรอกอีเมล"
            isValid = false
        }

        if (password.isEmpty()) {
            passwordEditText.error = "กรุณากรอกรหัสผ่าน"
            isValid = false
        }

        return isValid
    }

    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        loginButton.isEnabled = false
        forgotPasswordButton.isEnabled = false
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
        loginButton.isEnabled = true
        forgotPasswordButton.isEnabled = true
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun navigateToMainContainer() {
        startActivity(Intent(this, MainContainer::class.java))
        finish()
    }
}