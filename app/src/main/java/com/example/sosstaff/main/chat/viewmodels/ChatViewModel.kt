// com.kku.emergencystaff/main/chat/viewmodels/ChatViewModel.kt
package com.example.sosstaff.main.chat.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.sosstaff.main.chat.repository.ChatRepository
import com.example.sosstaff.models.ChatRoom
import com.example.sosstaff.models.Message

class ChatViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _selectedChatId = MutableLiveData<String>()
    val selectedChatId: LiveData<String> get() = _selectedChatId

    // LiveData for assigned chat rooms
    val assignedChatRooms: LiveData<List<ChatRoom>> = chatRepository.getAssignedChatRooms()

    // LiveData for unassigned chat rooms
    val unassignedChatRooms: LiveData<List<ChatRoom>> = chatRepository.getUnassignedChatRooms()

    // LiveData for chat room filter status
    private val _chatFilter = MutableLiveData<ChatFilter>(ChatFilter.ALL)
    val chatFilter: LiveData<ChatFilter> get() = _chatFilter

    // LiveData for filtered chat rooms
    private val _filteredChatRooms = MediatorLiveData<List<ChatRoom>>()
    val filteredChatRooms: LiveData<List<ChatRoom>> get() = _filteredChatRooms

    // LiveData for total unread count
    private val _totalUnreadCount = MediatorLiveData<Int>()
    val totalUnreadCount: LiveData<Int> get() = _totalUnreadCount

    // Messages in selected chat
    private val _currentChatMessages = MutableLiveData<List<Message>>()
    val currentChatMessages: LiveData<List<Message>> get() = _currentChatMessages

    // Current selected chat room
    private val _currentChatRoom = MutableLiveData<ChatRoom?>()
    val currentChatRoom: LiveData<ChatRoom?> get() = _currentChatRoom

    init {
        setupFilteredChatRooms()
        calculateTotalUnreadCount()
    }

    private fun setupFilteredChatRooms() {
        // Track changes in assigned chat rooms
        _filteredChatRooms.addSource(assignedChatRooms) { chatRooms ->
            updateFilteredChatRooms(chatRooms)
        }

        // Track changes in filter
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

    // Change chat filter
    fun setChatFilter(filter: ChatFilter) {
        _chatFilter.value = filter
    }

    // Select chat room
    fun selectChatRoom(chatId: String) {
        _selectedChatId.value = chatId

        // Get chat room data
        val chatRoomLiveData = chatRepository.getChatRoomById(chatId)

        // Observe selected chat room
        chatRoomLiveData.observeForever { chatRoom ->
            _currentChatRoom.value = chatRoom
        }

        // Get messages in chat room
        val messagesLiveData = chatRepository.getChatMessages(chatId)

        // Observe messages in chat room
        messagesLiveData.observeForever { messages ->
            _currentChatMessages.value = messages
        }
    }

    // Send new message
    fun sendMessage(chatId: String, message: String): LiveData<Boolean> {
        return chatRepository.sendMessage(chatId, message)
    }

    // Assign unassigned chat room
    fun assignChatRoom(chatId: String): LiveData<Boolean> {
        return chatRepository.assignChatRoom(chatId)
    }

    // Check if message is from current staff
    fun isMessageFromCurrentStaff(message: Message): Boolean {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return currentUser != null && message.senderId == currentUser.uid
    }

    // Check if chat room has unread messages
    fun hasUnreadMessages(chatRoom: ChatRoom): Boolean {
        return chatRoom.staffUnreadCount > 0
    }

    // Chat filter types
    enum class ChatFilter {
        ALL,        // All
        ACTIVE,     // Active
        COMPLETED   // Completed
    }

    // Mark messages as read
    fun markMessagesAsRead(chatId: String) {
        chatRepository.markMessagesAsRead(chatId)
    }
}