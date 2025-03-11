// พาธ: com.kku.emergencystaff/main/profile/viewmodels/ProfileViewModel.kt
package com.example.sosstaff.main.profile.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.sosstaff.main.profile.repository.ProfileRepository
import com.example.sosstaff.models.StaffUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    // LiveData สำหรับข้อมูลโปรไฟล์เจ้าหน้าที่
    val staffProfile: LiveData<StaffUser?> get() = profileRepository.getCurrentStaffProfile()

    // LiveData สำหรับผลลัพธ์การอัปเดตโปรไฟล์
    private val _updateProfileResult = MutableLiveData<Boolean>()
    val updateProfileResult: LiveData<Boolean> get() = _updateProfileResult

    // LiveData สำหรับผลลัพธ์การเปลี่ยนรหัสผ่าน
    private val _changePasswordResult = MutableLiveData<Boolean>()
    val changePasswordResult: LiveData<Boolean> get() = _changePasswordResult

    // LiveData สำหรับผลลัพธ์การออกจากระบบ
    private val _logoutResult = MutableLiveData<Boolean>()
    val logoutResult: LiveData<Boolean> get() = _logoutResult

    // อัปเดตข้อมูลโปรไฟล์
    fun updateProfile(name: String, phone: String) {
        profileRepository.updateStaffProfile(name, phone).observeForever { result ->
            _updateProfileResult.value = result
        }
    }

    // เปลี่ยนสถานะเจ้าหน้าที่
    fun changeStatus(status: String): LiveData<Boolean> {
        return profileRepository.updateStaffStatus(status)
    }

    // เปลี่ยนรหัสผ่าน
    fun changePassword(newPassword: String, confirmPassword: String) {
        if (newPassword != confirmPassword) {
            _changePasswordResult.value = false
            return
        }

        profileRepository.changePassword(newPassword).observeForever { result ->
            _changePasswordResult.value = result
        }
    }

    // ขอลิงก์รีเซ็ตรหัสผ่านทางอีเมล
    fun requestPasswordReset(): LiveData<Boolean> {
        return profileRepository.requestPasswordReset()
    }

    // ออกจากระบบ
    fun logout() {
        profileRepository.logout().observeForever { result ->
            _logoutResult.value = result
        }
    }

    // ตรวจสอบความถูกต้องของรหัสผ่านใหม่
    fun isValidPassword(password: String): Boolean {
        // รหัสผ่านต้องมีความยาวอย่างน้อย 8 ตัวและมีตัวอักษรตัวใหญ่ ตัวเล็ก และตัวเลข
        val passwordRegex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$".toRegex()
        return passwordRegex.matches(password)
    }
}