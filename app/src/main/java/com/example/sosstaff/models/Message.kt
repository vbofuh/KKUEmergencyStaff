// app/src/main/java/com/example/sosstaff/models/Message.kt

package com.example.sosstaff.models

import com.google.firebase.firestore.DocumentId
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Message(
    @DocumentId
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderType: String = "", // "user" or "staff"
    val message: String = "",
    val timestamp: Date = Date(),
    val isRead: Boolean = false
) {
    // Method for formatted time display
    fun getFormattedTime(): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return dateFormat.format(timestamp)
    }

    // Method for formatted date display (for headers)
    fun getFormattedDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(timestamp)
    }

    // Method to check if message is from current user
    fun isFromCurrentUser(currentUserId: String): Boolean {
        return senderId == currentUserId
    }

    // Constructor with no parameters for Firebase
    constructor() : this("", "", "", "", "", "", Date(), false)
}