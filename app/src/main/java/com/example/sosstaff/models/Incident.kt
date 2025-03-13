// app/src/main/java/com/example/sosstaff/models/Incident.kt

package com.example.sosstaff.models

import com.google.firebase.firestore.DocumentId
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Incident(
    @DocumentId
    val id: String = "",
    val reporterId: String = "",
    val reporterName: String = "",
    val reporterPhone: String = "",
    val incidentType: String = "", // อุบัติเหตุบนถนน, จับสัตว์, ทะเลาะวิวาท, etc.
    val location: String = "",
    val relationToVictim: String = "", // ผู้ประสบเหตุ, ผู้เห็นเหตุการณ์, เพื่อนผู้ประสบเหตุ
    val additionalInfo: String = "",
    val status: String = "รอรับเรื่อง", // รอรับเรื่อง, เจ้าหน้าที่รับเรื่องแล้ว, กำลังดำเนินการ, เสร็จสิ้น
    val assignedStaffId: String = "",
    val assignedStaffName: String = "",
    val reportedAt: Long = System.currentTimeMillis(),   // Changed from Date to Long
    val lastUpdatedAt: Long = System.currentTimeMillis(), // Changed from Date to Long
    val completedAt: Long? = null                         // Changed from Date? to Long?
) {
    // Method to check if the incident is still active
    fun isActive(): Boolean {
        return status != "เสร็จสิ้น"
    }

    // Method to format report time for display
    fun getFormattedReportTime(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return dateFormat.format(Date(reportedAt))
    }

    // Method to determine status color
    fun getStatusColor(): Int {
        return when (status) {
            "รอรับเรื่อง" -> 0xFFFF9800.toInt() // Orange
            "เจ้าหน้าที่รับเรื่องแล้ว" -> 0xFF2196F3.toInt() // Blue
            "กำลังดำเนินการ" -> 0xFF4CAF50.toInt() // Green
            "เสร็จสิ้น" -> 0xFF9E9E9E.toInt() // Gray
            else -> 0xFF000000.toInt() // Black
        }
    }
}