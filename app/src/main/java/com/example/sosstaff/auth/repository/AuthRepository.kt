// พาธ: com.kku.emergencystaff/auth/repository/AuthRepository.kt
package com.kku.emergencystaff.auth.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val staffCollection = "staff"

    sealed class AuthResult {
        object Loading : AuthResult()
        object Success : AuthResult()
        data class Error(val message: String) : AuthResult()
    }

    fun login(email: String, password: String): LiveData<AuthResult> {
        val resultLiveData = MutableLiveData<AuthResult>()
        resultLiveData.value = AuthResult.Loading

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val userId = authResult.user?.uid ?: return@addOnSuccessListener

                // ตรวจสอบว่าผู้ใช้เป็นเจ้าหน้าที่หรือไม่
                firestore.collection(staffCollection)
                    .document(userId)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            // อัปเดต FCM Token
                            updateFCMToken(userId)
                            resultLiveData.value = AuthResult.Success
                        } else {
                            auth.signOut()
                            resultLiveData.value = AuthResult.Error("คุณไม่ได้รับอนุญาตให้ใช้แอปพลิเคชันนี้")
                        }
                    }
                    .addOnFailureListener { e ->
                        auth.signOut()
                        resultLiveData.value = AuthResult.Error(e.message ?: "เกิดข้อผิดพลาดในการตรวจสอบสิทธิ์")
                    }
            }
            .addOnFailureListener { e ->
                resultLiveData.value = AuthResult.Error(e.message ?: "การเข้าสู่ระบบล้มเหลว")
            }

        return resultLiveData
    }

    fun resetPassword(email: String): LiveData<AuthResult> {
        val resultLiveData = MutableLiveData<AuthResult>()
        resultLiveData.value = AuthResult.Loading

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                resultLiveData.value = AuthResult.Success
            }
            .addOnFailureListener { e ->
                resultLiveData.value = AuthResult.Error(e.message ?: "การส่งอีเมลรีเซ็ตรหัสผ่านล้มเหลว")
            }

        return resultLiveData
    }

    fun logout(): LiveData<AuthResult> {
        val resultLiveData = MutableLiveData<AuthResult>()

        try {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                // ลบ FCM Token
                firestore.collection(staffCollection)
                    .document(userId)
                    .update("fcmToken", "")
                    .addOnCompleteListener {
                        auth.signOut()
                        resultLiveData.value = AuthResult.Success
                    }
            } else {
                auth.signOut()
                resultLiveData.value = AuthResult.Success
            }
        } catch (e: Exception) {
            resultLiveData.value = AuthResult.Error(e.message ?: "การออกจากระบบล้มเหลว")
        }

        return resultLiveData
    }

    fun getCurrentStaff(): LiveData<StaffUser?> {
        val staffLiveData = MutableLiveData<StaffUser?>()

        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection(staffCollection)
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val staff = document.toObject(StaffUser::class.java)
                        staffLiveData.value = staff
                    } else {
                        staffLiveData.value = null
                    }
                }
                .addOnFailureListener {
                    staffLiveData.value = null
                }
        } else {
            staffLiveData.value = null
        }

        return staffLiveData
    }

    private fun updateFCMToken(userId: String) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            firestore.collection(staffCollection)
                .document(userId)
                .update("fcmToken", token)
                .addOnFailureListener { /* อาจจะเพิ่มการจัดการข้อผิดพลาด */ }
        }
    }

    // โมเดลข้อมูลภายในอย่างง่าย
    data class StaffUser(
        val id: String = "",
        val name: String = "",
        val email: String = "",
        val phone: String = "",
        val position: String = "",
        val status: String = "ว่าง",
        val fcmToken: String = ""
    )
}