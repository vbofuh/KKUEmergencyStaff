// พาธ: com.kku.emergencystaff/MainActivity.kt
package com.example.sosstaff

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.sosstaff.auth.LoginActivity
import com.example.sosstaff.main.MainContainer

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        val db = FirebaseFirestore.getInstance()
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Check if user is signed in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // ตรวจสอบว่าผู้ใช้เป็นเจ้าหน้าที่
            checkIfUserIsStaff(currentUser.uid)
        } else {
            // ไม่มีผู้ใช้ล็อกอินอยู่
            navigateToLogin()
        }
    }

    private fun checkIfUserIsStaff(uid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("staff").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // ผู้ใช้เป็นเจ้าหน้าที่
                    navigateToMainContainer()
                } else {
                    // ผู้ใช้ไม่ใช่เจ้าหน้าที่
                    auth.signOut()
                    navigateToLogin()
                }
            }
            .addOnFailureListener {
                // เกิดข้อผิดพลาดในการตรวจสอบ
                auth.signOut()
                navigateToLogin()
            }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun navigateToMainContainer() {
        startActivity(Intent(this, MainContainer::class.java))
        finish()
    }
}