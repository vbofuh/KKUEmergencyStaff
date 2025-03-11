// พาธ: com.kku.emergencystaff/main/profile/repository/ProfileRepository.kt
package com.kku.emergencystaff.main.profile.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kku.emergencystaff.models.StaffUser
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val staffCollection = "staff"

    // ดึงข้อมูลโปรไฟล์เจ้าหน้าที่ปัจจุบัน
    fun getCurrentStaffProfile(): LiveData<StaffUser?> {
        val profileLiveData = MutableLiveData<StaffUser?>()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            profileLiveData.value = null
            return profileLiveData
        }

        firestore.collection(staffCollection)
            .document(currentUser.uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val staffUser = snapshot.toObject(StaffUser::class.java)
                    profileLiveData.value = staffUser

                    // อัปเดตเวลาที่ใช้งานล่าสุด
                    updateLastActive()
                } else {
                    profileLiveData.value = null
                }
            }

        return profileLiveData
    }

    // อัปเดตข้อมูลโปรไฟล์เจ้าหน้าที่
    fun updateStaffProfile(name: String, phone: String): LiveData<Boolean> {
        val resultLiveData = MutableLiveData<Boolean>()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            resultLiveData.value = false
            return resultLiveData
        }

        val updates = hashMapOf<String, Any>(
            "name" to name,
            "phone" to phone
        )

        firestore.collection(staffCollection)
            .document(currentUser.uid)
            .update(updates)
            .addOnSuccessListener {
                resultLiveData.value = true
            }
            .addOnFailureListener {
                resultLiveData.value = false
            }

        return resultLiveData
    }

    // อัปเดตสถานะของเจ้าหน้าที่
    fun updateStaffStatus(status: String): LiveData<Boolean> {
        val resultLiveData = MutableLiveData<Boolean>()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            resultLiveData.value = false
            return resultLiveData
        }

        firestore.collection(staffCollection)
            .document(currentUser.uid)
            .update("status", status)
            .addOnSuccessListener {
                resultLiveData.value = true
            }
            .addOnFailureListener {
                resultLiveData.value = false
            }

        return resultLiveData
    }

    // อัปเดตเวลาที่ใช้งานล่าสุด
    private fun updateLastActive() {
        val currentUser = auth.currentUser ?: return

        firestore.collection(staffCollection)
            .document(currentUser.uid)
            .update("lastActiveAt", Date())
    }

    // เปลี่ยนรหัสผ่าน
    fun changePassword(newPassword: String): LiveData<Boolean> {
        val resultLiveData = MutableLiveData<Boolean>()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            resultLiveData.value = false
            return resultLiveData
        }

        currentUser.updatePassword(newPassword)
            .addOnSuccessListener {
                resultLiveData.value = true
            }
            .addOnFailureListener {
                resultLiveData.value = false
            }

        return resultLiveData
    }

    // ขอลิงก์รีเซ็ตรหัสผ่านทางอีเมล
    fun requestPasswordReset(): LiveData<Boolean> {
        val resultLiveData = MutableLiveData<Boolean>()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            resultLiveData.value = false
            return resultLiveData
        }

        val email = currentUser.email
        if (email.isNullOrEmpty()) {
            resultLiveData.value = false
            return resultLiveData
        }

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                resultLiveData.value = true
            }
            .addOnFailureListener {
                resultLiveData.value = false
            }

        return resultLiveData
    }

    // ออกจากระบบ
    fun logout(): LiveData<Boolean> {
        val resultLiveData = MutableLiveData<Boolean>()
        val currentUser = auth.currentUser

        try {
            if (currentUser != null) {
                // ลบ FCM Token
                firestore.collection(staffCollection)
                    .document(currentUser.uid)
                    .update("fcmToken", "")
                    .addOnCompleteListener {
                        auth.signOut()
                        resultLiveData.value = true
                    }
            } else {
                auth.signOut()
                resultLiveData.value = true
            }
        } catch (e: Exception) {
            resultLiveData.value = false
        }

        return resultLiveData
    }
}