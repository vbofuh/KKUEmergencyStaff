// app/src/main/java/com/example/sosstaff/main/chat/repository/ChatRepository.kt

package com.example.sosstaff.main.chat.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.sosstaff.models.ChatRoom
import com.example.sosstaff.models.Message
import com.google.firebase.firestore.FieldValue
import java.util.Date

class ChatRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val chatRoomsCollection = firestore.collection("chats")
    private val messagesCollection = firestore.collection("messages")
    private val staffCollection = firestore.collection("staff")
    private val incidentsCollection = firestore.collection("incidents")

    // Fix for sending messages
    fun sendMessage(chatId: String, messageText: String): LiveData<Boolean> {
        val resultLiveData = MutableLiveData<Boolean>()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            resultLiveData.value = false
            return resultLiveData
        }

        // Check the chat room first
        chatRoomsCollection.document(chatId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val chatRoom = document.toObject(ChatRoom::class.java)

                    if (chatRoom != null && chatRoom.active) {
                        // Get staff information
                        staffCollection.document(currentUser.uid)
                            .get()
                            .addOnSuccessListener { staffDoc ->
                                if (staffDoc.exists()) {
                                    val staffName = staffDoc.getString("name") ?: "เจ้าหน้าที่"

                                    // Create new message
                                    val newMessage = Message(
                                        chatId = chatId,
                                        senderId = currentUser.uid,
                                        senderName = staffName,
                                        senderType = "staff",
                                        message = messageText,
                                        timestamp = Date(),
                                        isRead = false
                                    )

                                    // Add new message
                                    messagesCollection
                                        .add(newMessage)
                                        .addOnSuccessListener {
                                            // Update latest data in chat room
                                            updateChatRoomLastMessage(chatId, messageText, newMessage.timestamp, true)
                                            resultLiveData.value = true
                                        }
                                        .addOnFailureListener {
                                            resultLiveData.value = false
                                        }
                                } else {
                                    resultLiveData.value = false
                                }
                            }
                            .addOnFailureListener {
                                resultLiveData.value = false
                            }
                    } else {
                        resultLiveData.value = false
                    }
                } else {
                    resultLiveData.value = false
                }
            }
            .addOnFailureListener {
                resultLiveData.value = false
            }

        return resultLiveData
    }

    // Fix for assigning unassigned chat rooms
    fun assignChatRoom(chatId: String): LiveData<Boolean> {
        val resultLiveData = MutableLiveData<Boolean>()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            resultLiveData.value = false
            return resultLiveData
        }

        // Get staff info
        staffCollection.document(currentUser.uid)
            .get()
            .addOnSuccessListener { staffDoc ->
                if (staffDoc.exists()) {
                    val staffName = staffDoc.getString("name") ?: "เจ้าหน้าที่"

                    // Update chat room
                    chatRoomsCollection.document(chatId)
                        .update(
                            mapOf(
                                "staffId" to currentUser.uid,
                                "staffName" to staffName,
                                "staffUnreadCount" to 0 // Reset unread count when assigning
                            )
                        )
                        .addOnSuccessListener {
                            // Also update incident
                            incidentsCollection.document(chatId) // Using same ID
                                .update(
                                    mapOf(
                                        "assignedStaffId" to currentUser.uid,
                                        "assignedStaffName" to staffName,
                                        "status" to "เจ้าหน้าที่รับเรื่องแล้ว",
                                        "lastUpdatedAt" to Date()
                                    )
                                )
                                .addOnSuccessListener {
                                    resultLiveData.value = true
                                }
                                .addOnFailureListener {
                                    resultLiveData.value = false
                                }
                        }
                        .addOnFailureListener {
                            resultLiveData.value = false
                        }
                } else {
                    resultLiveData.value = false
                }
            }
            .addOnFailureListener {
                resultLiveData.value = false
            }

        return resultLiveData
    }

    // Fix for updating read status of messages
    private fun updateChatRoomLastMessage(chatId: String, message: String, timestamp: Date, isFromStaff: Boolean) {
        val updates = hashMapOf<String, Any>(
            "lastMessage" to message,
            "lastMessageTime" to timestamp
        )

        // If the message is from staff, increase unread count for user
        if (isFromStaff) {
            chatRoomsCollection.document(chatId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userUnreadCount = document.getLong("userUnreadCount") ?: 0
                        updates["userUnreadCount"] = userUnreadCount + 1

                        chatRoomsCollection.document(chatId)
                            .update(updates)
                    }
                }
        } else {
            // If message is from user, increase unread count for staff
            chatRoomsCollection.document(chatId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val staffUnreadCount = document.getLong("staffUnreadCount") ?: 0
                        updates["staffUnreadCount"] = staffUnreadCount + 1

                        chatRoomsCollection.document(chatId)
                            .update(updates)
                    }
                }
        }
    }

    // Fix for markMessagesAsRead method
    fun markMessagesAsRead(chatId: String) {
        val userId = auth.currentUser?.uid ?: return

        // Find messages in this chat that were not sent by current user and are unread
        messagesCollection
            .whereEqualTo("chatId", chatId)
            .whereNotEqualTo("senderId", userId)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Mark each message as read
                for (document in querySnapshot.documents) {
                    document.reference.update("isRead", true)
                }

                // Reset the unread counter in the chat room
                if (querySnapshot.documents.isNotEmpty()) {
                    chatRoomsCollection.document(chatId)
                        .update("staffUnreadCount", 0)
                }
            }
    }

    fun getAssignedChatRooms(): LiveData<List<ChatRoom>> {
        val chatRoomsLiveData = MutableLiveData<List<ChatRoom>>()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            chatRoomsLiveData.value = emptyList()
            return chatRoomsLiveData
        }

        chatRoomsCollection
            .whereEqualTo("staffId", currentUser.uid)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    chatRoomsLiveData.value = emptyList()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val chatRooms = snapshot.toObjects(ChatRoom::class.java)
                    chatRoomsLiveData.value = chatRooms
                } else {
                    chatRoomsLiveData.value = emptyList()
                }
            }

        return chatRoomsLiveData
    }

    // Get unassigned chat rooms
    fun getUnassignedChatRooms(): LiveData<List<ChatRoom>> {
        val chatRoomsLiveData = MutableLiveData<List<ChatRoom>>()

        chatRoomsCollection
            .whereEqualTo("staffId", "")
            .whereEqualTo("active", true)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    chatRoomsLiveData.value = emptyList()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val chatRooms = snapshot.toObjects(ChatRoom::class.java)
                    chatRoomsLiveData.value = chatRooms
                } else {
                    chatRoomsLiveData.value = emptyList()
                }
            }

        return chatRoomsLiveData
    }

    // Get chat room by ID
    fun getChatRoomById(chatId: String): LiveData<ChatRoom?> {
        val chatRoomLiveData = MutableLiveData<ChatRoom?>()

        if (chatId.isEmpty()) {
            chatRoomLiveData.value = null
            return chatRoomLiveData
        }

        chatRoomsCollection.document(chatId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    chatRoomLiveData.value = null
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val chatRoom = snapshot.toObject(ChatRoom::class.java)
                    chatRoomLiveData.value = chatRoom
                } else {
                    chatRoomLiveData.value = null
                }
            }

        return chatRoomLiveData
    }

    // Get messages in chat room
    fun getChatMessages(chatId: String): LiveData<List<Message>> {
        val messagesLiveData = MutableLiveData<List<Message>>()
        val currentUser = auth.currentUser

        if (chatId.isEmpty()) {
            messagesLiveData.value = emptyList()
            return messagesLiveData
        }

        messagesCollection
            .whereEqualTo("chatId", chatId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    messagesLiveData.value = emptyList()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val messages = snapshot.toObjects(Message::class.java)
                    messagesLiveData.value = messages

                    // Update read status if current user exists
                    if (currentUser != null) {
                        markMessagesAsRead(chatId)
                    }
                } else {
                    messagesLiveData.value = emptyList()
                }
            }

        return messagesLiveData
    }
}