// app/src/main/java/com/example/sosstaff/models/ChatRoom.kt

package com.example.sosstaff.models

import com.google.firebase.firestore.DocumentId
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatRoom(
    @DocumentId
    val id: String = "", // Use incidentId as chat room id
    val incidentId: String = "",
    val incidentType: String = "",
    val userId: String = "", // Reporter's ID
    val userName: String = "", // Reporter's name
    val staffId: String = "", // Staff's ID
    val staffName: String = "", // Staff's name
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val staffUnreadCount: Int = 0,
    val userUnreadCount: Int = 0,
    val active: Boolean = true // Whether the chat room is still active (incident not completed)
) {
    // Method to check if this chat room has a staff assigned
    fun hasStaff(): Boolean {
        return staffId.isNotEmpty()
    }

    // Method to check if there are unread messages (for staff)
    fun hasUnreadMessages(): Boolean {
        return staffUnreadCount > 0
    }

    // Method for formatted last message time
    fun getFormattedLastMessageTime(): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(Date(lastMessageTime))
    }

    // Method to get status text
    fun getStatusText(): String {
        return if (active) "กำลังสนทนา" else "สิ้นสุดการสนทนา"
    }

    // Method to get status color
    fun getStatusColor(): Int {
        return if (active) 0xFF4CAF50.toInt() else 0xFF9E9E9E.toInt()
    }
}