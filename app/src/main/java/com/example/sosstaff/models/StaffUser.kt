// พาธ: com.kku.emergencystaff/models/StaffUser.kt
package com.kku.emergencystaff.models

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class StaffUser(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val position: String = "", // ตำแหน่ง (เช่น เจ้าหน้าที่ความปลอดภัย, พยาบาล)
    val status: String = "ว่าง", // ว่าง, กำลังทำงาน
    val imageUrl: String = "",
    val fcmToken: String = "",
    val lastActiveAt: Date = Date(),
    val createdAt: Date = Date()
) {
    // เมธอดสำหรับตรวจสอบว่าเจ้าหน้าที่พร้อมรับเหตุการณ์ใหม่หรือไม่
    fun isAvailable(): Boolean {
        return status == "ว่าง"
    }

    // เมธอดสำหรับตรวจสอบว่าเจ้าหน้าที่ออนไลน์อยู่หรือไม่ (ออนไลน์ภายใน 15 นาทีล่าสุด)
    fun isOnline(): Boolean {
        val fifteenMinutesAgo = Date(System.currentTimeMillis() - 15 * 60 * 1000)
        return lastActiveAt.after(fifteenMinutesAgo)
    }

    // เมธอดสำหรับแสดงชื่อเต็ม (ใช้ในกรณีที่ชื่อมี format เป็น "ชื่อ นามสกุล")
    fun getFirstName(): String {
        return name.split(" ").firstOrNull() ?: ""
    }

    fun getStatusText(): String {
        return when {
            !isOnline() -> "ออฟไลน์"
            status == "ว่าง" -> "ว่าง"
            else -> "กำลังทำงาน"
        }
    }

    fun getStatusColor(): Int {
        return when {
            !isOnline() -> 0xFF9E9E9E.toInt() // สีเทา
            status == "ว่าง" -> 0xFF4CAF50.toInt() // สีเขียว
            else -> 0xFF2196F3.toInt() // สีฟ้า
        }
    }
}