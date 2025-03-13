// app/src/main/java/com/example/sosstaff/models/StaffUser.kt

package com.example.sosstaff.models

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
    val fcmToken: String = "",
    val lastActiveAt: Date = Date(),
    val createdAt: Date = Date()
) {
    // Method to check if staff is available
    fun isAvailable(): Boolean {
        return status == "ว่าง"
    }

    // Method to check if staff is online (active in last 15 minutes)
    fun isOnline(): Boolean {
        val fifteenMinutesAgo = Date(System.currentTimeMillis() - 15 * 60 * 1000)
        return lastActiveAt.after(fifteenMinutesAgo)
    }

    // Method to get status text
    fun getStatusText(): String {
        return when {
            !isOnline() -> "ออฟไลน์"
            status == "ว่าง" -> "ว่าง"
            else -> "กำลังทำงาน"
        }
    }

    // Method to get status color
    fun getStatusColor(): Int {
        return when {
            !isOnline() -> 0xFF9E9E9E.toInt() // Gray
            status == "ว่าง" -> 0xFF4CAF50.toInt() // Green
            else -> 0xFF2196F3.toInt() // Blue
        }
    }
}