// พาธ: com.kku.emergencystaff/models/ChatRoom.kt
package com.example.sosstaff.models

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class ChatRoom(
    @DocumentId
    val id: String = "", // ใช้ incidentId เป็น id ของห้องแชท
    val incidentId: String = "",
    val incidentType: String = "", // ประเภทเหตุการณ์
    val userId: String = "", // ID ของผู้แจ้งเหตุ
    val userName: String = "", // ชื่อผู้แจ้งเหตุ
    val staffId: String = "", // ID ของเจ้าหน้าที่
    val staffName: String = "", // ชื่อเจ้าหน้าที่
    val lastMessage: String = "",
    val lastMessageTime: Date = Date(),
    val userUnreadCount: Int = 0, // จำนวนข้อความที่ผู้ใช้ยังไม่ได้อ่าน
    val staffUnreadCount: Int = 0, // จำนวนข้อความที่เจ้าหน้าที่ยังไม่ได้อ่าน
    val active: Boolean = true // ห้องแชทยังใช้งานได้หรือไม่
) {
    // เมธอดสำหรับตรวจสอบว่าห้องแชทนี้มีเจ้าหน้าที่รับผิดชอบแล้วหรือยัง
    fun hasStaff(): Boolean {
        return staffId.isNotEmpty()
    }

    // เมธอดสำหรับตรวจสอบว่าห้องแชทนี้มีข้อความที่ยังไม่ได้อ่านหรือไม่ (สำหรับเจ้าหน้าที่)
    fun hasUnreadMessages(): Boolean {
        return staffUnreadCount > 0
    }

    // เมธอดสำหรับจัดรูปแบบเวลาข้อความล่าสุด
    fun getFormattedLastMessageTime(): String {
        // สร้างรูปแบบเวลาที่เหมาะสม (อาจใช้ DateUtils จากโค้ดก่อนหน้า)
        return com.kku.emergencystaff.common.utils.DateUtils.getRelativeTimeSpan(lastMessageTime.time)
    }

    // เมธอดสำหรับตรวจสอบว่าข้อความล่าสุดส่งโดยเจ้าหน้าที่คนนี้หรือไม่
    fun isLastMessageFromCurrentStaff(currentStaffId: String): Boolean {
        return staffId == currentStaffId && staffId.isNotEmpty()
    }

    // เมธอดสำหรับแปลงสถานะการใช้งานเป็นข้อความ
    fun getStatusText(): String {
        return if (active) "กำลังสนทนา" else "สิ้นสุดการสนทนา"
    }

    // เมธอดสำหรับแปลงสถานะการใช้งานเป็นรหัสสี
    fun getStatusColor(): Int {
        return if (active) 0xFF4CAF50.toInt() else 0xFF9E9E9E.toInt()
    }
}