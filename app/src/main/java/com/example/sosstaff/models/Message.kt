// พาธ: com.kku.emergencystaff/models/Message.kt
package com.example.sosstaff.models

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Message(
    @DocumentId
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderType: String = "", // "user" หรือ "staff"
    val message: String = "",
    val timestamp: Date = Date(),
    val isRead: Boolean = false,
    val imageUrl: String = "" // สำหรับข้อความที่มีรูปภาพ (ถ้ามี)
) {
    // เมธอดสำหรับตรวจสอบว่าข้อความนี้ส่งโดยผู้ใช้คนปัจจุบันหรือไม่ (ใช้ในการแสดงข้อความในแชท)
    fun isFromCurrentUser(currentUserId: String): Boolean {
        return senderId == currentUserId
    }

    // เมธอดสำหรับตรวจสอบว่าข้อความนี้ส่งโดยเจ้าหน้าที่หรือไม่
    fun isFromStaff(): Boolean {
        return senderType == "staff"
    }

    // เมธอดสำหรับจัดรูปแบบเวลาข้อความ
    fun getFormattedTime(): String {
        return com.kku.emergencystaff.common.utils.DateUtils.formatTime(timestamp.time)
    }

    // เมธอดสำหรับจัดรูปแบบวันที่ข้อความ (สำหรับแสดงในส่วนหัวของวัน)
    fun getFormattedDate(): String {
        return com.kku.emergencystaff.common.utils.DateUtils.formatDateForHeader(timestamp.time)
    }

    // เมธอดสำหรับตรวจสอบว่าข้อความนี้มีรูปภาพหรือไม่
    fun hasImage(): Boolean {
        return imageUrl.isNotEmpty()
    }

    // เมธอดสำหรับเปรียบเทียบว่าข้อความนี้อยู่ในวันเดียวกับอีกข้อความหรือไม่ (ใช้ในการจัดกลุ่มตามวัน)
    fun isSameDay(otherMessage: Message): Boolean {
        val cal1 = java.util.Calendar.getInstance()
        val cal2 = java.util.Calendar.getInstance()
        cal1.time = timestamp
        cal2.time = otherMessage.timestamp

        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }
}