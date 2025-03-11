// พาธ: com.kku.emergencystaff/models/Incident.kt
package com.kku.emergencystaff.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import java.util.Date

data class Incident(
    @DocumentId
    val id: String = "",
    val reporterId: String = "",
    val reporterName: String = "",
    val reporterPhone: String = "",
    val incidentType: String = "", // อุบัติเหตุบนถนน, จับสัตว์, ทะเลาะวิวาท, etc.
    val location: String = "",
    val locationLatLng: GeoPoint? = null,
    val relationToVictim: String = "", // ผู้ประสบเหตุ, ผู้เห็นเหตุการณ์, เพื่อนผู้ประสบเหตุ
    val additionalInfo: String = "",
    val status: String = "รอรับเรื่อง", // รอรับเรื่อง, เจ้าหน้าที่รับเรื่องแล้ว, กำลังดำเนินการ, เสร็จสิ้น
    val assignedStaffId: String = "",
    val assignedStaffName: String = "",
    val reportedAt: Date = Date(),
    val lastUpdatedAt: Date = Date(),
    val completedAt: Date? = null
) {
    // เมธอดสำหรับตรวจสอบว่าเหตุการณ์ยังดำเนินการอยู่หรือไม่
    fun isActive(): Boolean {
        return status != "เสร็จสิ้น"
    }

    // เมธอดสำหรับตรวจสอบว่าเหตุการณ์นี้รับเรื่องแล้วหรือยัง
    fun isAssigned(): Boolean {
        return assignedStaffId.isNotEmpty()
    }

    // เมธอดสำหรับคำนวณระยะเวลาดำเนินการ
    fun getDuration(): Long {
        return when {
            completedAt != null -> completedAt.time - reportedAt.time
            else -> System.currentTimeMillis() - reportedAt.time
        }
    }

    // แปลงสถานะเป็นรหัสสี
    fun getStatusColor(): Int {
        return when (status) {
            "รอรับเรื่อง" -> 0xFFFF9800.toInt() // สีส้ม
            "เจ้าหน้าที่รับเรื่องแล้ว" -> 0xFF2196F3.toInt() // สีฟ้า
            "กำลังดำเนินการ" -> 0xFF4CAF50.toInt() // สีเขียว
            "เสร็จสิ้น" -> 0xFF9E9E9E.toInt() // สีเทา
            else -> 0xFF000000.toInt() // สีดำ
        }
    }
}