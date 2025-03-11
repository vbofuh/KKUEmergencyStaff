// พาธ: com.kku.emergencystaff/main/chat/viewmodels/ChatViewModel.kt
package com.example.sosstaff.main.chat.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.sosstaff.main.chat.repository.ChatRepository
import com.example.sosstaff.models.ChatRoom
import com.example.sosstaff.models.Message
import javax.inject.Inject


class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _selectedChatId = MutableLiveData<String>()
    val selectedChatId: LiveData<String> get() = _selectedChatId

    // LiveData สำหรับห้องแชทที่เจ้าหน้าที่รับผิดชอบ
    val assignedChatRooms: LiveData<List<ChatRoom>> get() = chatRepository.getChatRooms()

    // LiveData สำหรับห้องแชทที่ยังไม่มีเจ้าหน้าที่รับผิดชอบ
    val unassignedChatRooms: LiveData<List<ChatRoom>> get() = chatRepository.getUnassignedChatRooms()

    // LiveData สำหรับการกรองสถานะห้องแชท
    private val _chatFilter = MutableLiveData<ChatFilter>(ChatFilter.ALL)
    val chatFilter: LiveData<ChatFilter> get() = _chatFilter

    // LiveData สำหรับห้องแชทที่กรองแล้ว
    private val _filteredChatRooms = MediatorLiveData<List<ChatRoom>>()
    val filteredChatRooms: LiveData<List<ChatRoom>> get() = _filteredChatRooms

    // LiveData สำหรับจำนวนข้อความที่ยังไม่ได้อ่าน
    private val _totalUnreadCount = MediatorLiveData<Int>()
    val totalUnreadCount: LiveData<Int> get() = _totalUnreadCount

    // ข้อความในห้องแชทที่เลือก
    private val _currentChatMessages = MutableLiveData<List<Message>>()
    val currentChatMessages: LiveData<List<Message>> get() = _currentChatMessages

    // ห้องแชทที่เลือกในปัจจุบัน
    private val _currentChatRoom = MutableLiveData<ChatRoom?>()
    val currentChatRoom: LiveData<ChatRoom?> get() = _currentChatRoom

    init {
        setupFilteredChatRooms()
        calculateTotalUnreadCount()
    }

    private fun setupFilteredChatRooms() {
        // ติดตามการเปลี่ยนแปลงของห้องแชทที่รับผิดชอบ
        _filteredChatRooms.addSource(assignedChatRooms) { chatRooms ->
            updateFilteredChatRooms(chatRooms)
        }

        // ติดตามการเปลี่ยนแปลงของการกรอง
        _filteredChatRooms.addSource(_chatFilter) { filter ->
            val currentChatRooms = assignedChatRooms.value ?: emptyList()
            updateFilteredChatRooms(currentChatRooms)
        }
    }

    private fun updateFilteredChatRooms(chatRooms: List<ChatRoom>) {
        val filter = _chatFilter.value ?: ChatFilter.ALL

        _filteredChatRooms.value = when (filter) {
            ChatFilter.ALL -> chatRooms
            ChatFilter.ACTIVE -> chatRooms.filter { it.active }
            ChatFilter.COMPLETED -> chatRooms.filter { !it.active }
        }
    }

    private fun calculateTotalUnreadCount() {
        _totalUnreadCount.addSource(assignedChatRooms) { chatRooms ->
            val count = chatRooms.sumOf { it.staffUnreadCount }
            _totalUnreadCount.value = count
        }
    }

    // เปลี่ยนตัวกรองห้องแชท
    fun setChatFilter(filter: ChatFilter) {
        _chatFilter.value = filter
    }

    // เลือกห้องแชท
    fun selectChatRoom(chatId: String) {
        _selectedChatId.value = chatId

        // ดึงข้อมูลห้องแชท
        val chatRoomLiveData = chatRepository.getChatRoomById(chatId)

        // สังเกตการณ์ห้องแชทที่เลือก
        chatRoomLiveData.observeForever { chatRoom ->
            _currentChatRoom.value = chatRoom
        }

        // ดึงข้อความในห้องแชท
        val messagesLiveData = chatRepository.getMessages(chatId)

        // สังเกตการณ์ข้อความในห้องแชท
        messagesLiveData.observeForever { messages ->
            _currentChatMessages.value = messages
        }
    }

    // ส่งข้อความใหม่
    fun sendMessage(chatId: String, message: String): LiveData<Boolean> {
        return chatRepository.sendMessage(chatId, message)
    }

    // รับห้องแชทที่ยังไม่มีเจ้าหน้าที่
    fun assignChatRoom(chatId: String): LiveData<Boolean> {
        return chatRepository.assignChatRoom(chatId)
    }

    // เช็คว่าข้อความนี้เป็นของเจ้าหน้าที่คนปัจจุบันหรือไม่
    fun isMessageFromCurrentStaff(message: Message): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser != null && message.senderId == currentUser.uid
    }

    // เช็คว่าห้องแชทนี้มีข้อความที่ยังไม่ได้อ่านหรือไม่
    fun hasUnreadMessages(chatRoom: ChatRoom): Boolean {
        return chatRoom.staffUnreadCount > 0
    }

    // ประเภทตัวกรองห้องแชท
    enum class ChatFilter {
        ALL,        // ทั้งหมด
        ACTIVE,     // ใช้งานอยู่
        COMPLETED   // เสร็จสิ้นแล้ว
    }
}