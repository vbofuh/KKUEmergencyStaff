// พาธ: com.kku.emergencystaff/main/chat/repository/ChatRepository.kt
package com.example.sosstaff.main.chat.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.sosstaff.models.ChatRoom
import com.example.sosstaff.models.Message
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val chatRoomsCollection = "chats"
    private val messagesCollection = "messages"

    // ดึงรายการห้องแชททั้งหมดของเจ้าหน้าที่
    fun getChatRooms(): LiveData<List<ChatRoom>> {
        val chatRoomsLiveData = MutableLiveData<List<ChatRoom>>()
        val currentUser = auth.currentUser ?: return chatRoomsLiveData

        firestore.collection(chatRoomsCollection)
            .whereEqualTo("staffId", currentUser.uid)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val chatRooms = snapshot.toObjects(ChatRoom::class.java)
                    chatRoomsLiveData.value = chatRooms
                }
            }

        return chatRoomsLiveData
    }

    // ดึงรายการห้องแชทที่ยังไม่มีเจ้าหน้าที่รับผิดชอบ
    fun getUnassignedChatRooms(): LiveData<List<ChatRoom>> {
        val chatRoomsLiveData = MutableLiveData<List<ChatRoom>>()

        firestore.collection(chatRoomsCollection)
            .whereEqualTo("staffId", "")
            .whereEqualTo("active", true)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val chatRooms = snapshot.toObjects(ChatRoom::class.java)
                    chatRoomsLiveData.value = chatRooms
                }
            }

        return chatRoomsLiveData
    }

    // ดึงรายละเอียดของห้องแชทตาม ID
    fun getChatRoomById(chatId: String): LiveData<ChatRoom?> {
        val chatRoomLiveData = MutableLiveData<ChatRoom?>()

        firestore.collection(chatRoomsCollection)
            .document(chatId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
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

    // ดึงข้อความทั้งหมดในห้องแชท
    fun getMessages(chatId: String): LiveData<List<Message>> {
        val messagesLiveData = MutableLiveData<List<Message>>()
        val currentUser = auth.currentUser

        firestore.collection(messagesCollection)
            .whereEqualTo("chatId", chatId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val messages = snapshot.toObjects(Message::class.java)
                    messagesLiveData.value = messages

                    // อัปเดตสถานะการอ่านข้อความ
                    if (currentUser != null) {
                        updateMessagesReadStatus(chatId, currentUser.uid)
                    }
                }
            }

        return messagesLiveData
    }

    // ส่งข้อความใหม่
    fun sendMessage(chatId: String, messageText: String): LiveData<Boolean> {
        val resultLiveData = MutableLiveData<Boolean>()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            resultLiveData.value = false
            return resultLiveData
        }

        // ตรวจสอบห้องแชทก่อน
        firestore.collection(chatRoomsCollection)
            .document(chatId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val chatRoom = document.toObject(ChatRoom::class.java)

                    if (chatRoom != null && chatRoom.active) {
                        // ดึงข้อมูลเจ้าหน้าที่
                        firestore.collection("staff")
                            .document(currentUser.uid)
                            .get()
                            .addOnSuccessListener { staffDoc ->
                                if (staffDoc.exists()) {
                                    val staffName = staffDoc.getString("name") ?: "เจ้าหน้าที่"

                                    // สร้างข้อความใหม่
                                    val newMessage = Message(
                                        chatId = chatId,
                                        senderId = currentUser.uid,
                                        senderName = staffName,
                                        senderType = "staff",
                                        message = messageText,
                                        timestamp = Date(),
                                        isRead = false
                                    )

                                    // เพิ่มข้อความใหม่
                                    firestore.collection(messagesCollection)
                                        .add(newMessage)
                                        .addOnSuccessListener {
                                            // อัปเดตข้อมูลล่าสุดในห้องแชท
                                            updateChatRoomLastMessage(chatId, messageText, newMessage.timestamp, true)
                                            resultLiveData.value = true
                                        }
                                        .addOnFailureListener {
                                            resultLiveData.value = false
                                        }
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

    // รับห้องแชทที่ยังไม่มีเจ้าหน้าที่
    fun assignChatRoom(chatId: String): LiveData<Boolean> {
        val resultLiveData = MutableLiveData<Boolean>()
        val currentUser = auth.currentUser

        if (currentUser == null) {
            resultLiveData.value = false
            return resultLiveData
        }

        // ดึงข้อมูลเจ้าหน้าที่
        firestore.collection("staff")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { staffDoc ->
                if (staffDoc.exists()) {
                    val staffName = staffDoc.getString("name") ?: "เจ้าหน้าที่"

                    // อัปเดตข้อมูลในห้องแชท
                    firestore.collection(chatRoomsCollection)
                        .document(chatId)
                        .update(
                            mapOf(
                                "staffId" to currentUser.uid,
                                "staffName" to staffName
                            )
                        )
                        .addOnSuccessListener {
                            // อัปเดตข้อมูลในเหตุการณ์ด้วย
                            firestore.collection("incidents")
                                .document(chatId) // ใช้ ID เดียวกัน
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

    // อัปเดตสถานะการอ่านข้อความ
    private fun updateMessagesReadStatus(chatId: String, currentUserId: String) {
        firestore.collection(messagesCollection)
            .whereEqualTo("chatId", chatId)
            .whereNotEqualTo("senderId", currentUserId) // เฉพาะข้อความที่ไม่ได้ส่งโดยเจ้าหน้าที่คนนี้
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.update("isRead", true)
                }

                // รีเซ็ต staffUnreadCount ในห้องแชท
                if (documents.size() > 0) {
                    firestore.collection(chatRoomsCollection)
                        .document(chatId)
                        .update("staffUnreadCount", 0)
                }
            }
    }

    // อัปเดตข้อมูลล่าสุดในห้องแชท
    private fun updateChatRoomLastMessage(chatId: String, message: String, timestamp: Date, isFromStaff: Boolean) {
        val updates = hashMapOf<String, Any>(
            "lastMessage" to message,
            "lastMessageTime" to timestamp
        )

        // ถ้าข้อความมาจากเจ้าหน้าที่ ให้เพิ่มจำนวนข้อความที่ยังไม่ได้อ่านของฝั่งผู้ใช้
        if (isFromStaff) {
            firestore.collection(chatRoomsCollection)
                .document(chatId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userUnreadCount = document.getLong("userUnreadCount") ?: 0
                        updates["userUnreadCount"] = userUnreadCount + 1

                        firestore.collection(chatRoomsCollection)
                            .document(chatId)
                            .update(updates)
                    }
                }
        } else {
            // ถ้าข้อความมาจากผู้ใช้ ให้เพิ่มจำนวนข้อความที่ยังไม่ได้อ่านของฝั่งเจ้าหน้าที่
            firestore.collection(chatRoomsCollection)
                .document(chatId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val staffUnreadCount = document.getLong("staffUnreadCount") ?: 0
                        updates["staffUnreadCount"] = staffUnreadCount + 1

                        firestore.collection(chatRoomsCollection)
                            .document(chatId)
                            .update(updates)
                    }
                }
        }
    }
}