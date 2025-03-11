// พาธ: com.kku.emergencystaff/main/chat/ChatActivity.kt
package com.example.sosstaff.main.chat

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.example.sosstaff.databinding.ActivityChatBinding
import com.example.sosstaff.main.chat.adapters.MessageAdapter
import com.example.sosstaff.main.chat.viewmodels.ChatViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CHAT_ID = "extra_chat_id"
        const val EXTRA_INCIDENT_TYPE = "extra_incident_type"
    }

    private lateinit var binding: ActivityChatBinding
    private val viewModel: ChatViewModel by viewModels()

    private lateinit var messageAdapter: MessageAdapter
    private var chatId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // รับ ID ของห้องแชทจาก Intent
        chatId = intent.getStringExtra(EXTRA_CHAT_ID) ?: ""
        if (chatId.isEmpty()) {
            Toast.makeText(this, "ไม่พบข้อมูลห้องแชท", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        setupRecyclerView()
        setupSendButton()
        observeViewModel()

        // เลือกห้องแชท
        viewModel.selectChatRoom(chatId)
    }

    private fun setupUI() {
        // ตั้งค่าหัวข้อ
        val incidentType = intent.getStringExtra(EXTRA_INCIDENT_TYPE)
        if (incidentType != null) {
            binding.tvChatTitle.text = incidentType
        }

        // ตั้งค่าปุ่มย้อนกลับ
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        messageAdapter = MessageAdapter(currentUserId)
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true // แสดงข้อความล่าสุดด้านล่าง
            }
            adapter = messageAdapter
        }
    }

    private fun setupSendButton() {
        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
            }
        }
    }

    private fun observeViewModel() {
        // สังเกตการณ์ข้อความในห้องแชท
        viewModel.currentChatMessages.observe(this) { messages ->
            messageAdapter.submitMessageList(messages)
            scrollToBottom()
        }

        // สังเกตการณ์ห้องแชทที่เลือก
        viewModel.currentChatRoom.observe(this) { chatRoom ->
            if (chatRoom != null) {
                // อัปเดตหัวข้อ
                binding.tvChatTitle.text = chatRoom.incidentType
                binding.tvChatSubtitle.text = "กำลังสนทนากับ: ${chatRoom.userName}"

                // แสดงข้อความแจ้งเตือนถ้าห้องแชทนี้ไม่ใช้งานแล้ว
                if (!chatRoom.active) {
                    binding.inactiveLayout.visibility = View.VISIBLE
                    binding.inputLayout.visibility = View.GONE
                } else {
                    binding.inactiveLayout.visibility = View.GONE
                    binding.inputLayout.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun sendMessage(message: String) {
        binding.btnSend.isEnabled = false

        viewModel.sendMessage(chatId, message).observe(this) { success ->
            binding.btnSend.isEnabled = true

            if (success) {
                binding.etMessage.text.clear()
                scrollToBottom()
            } else {
                Toast.makeText(this, "ไม่สามารถส่งข้อความได้", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun scrollToBottom() {
        if (messageAdapter.itemCount > 0) {
            binding.rvMessages.smoothScrollToPosition(messageAdapter.itemCount - 1)
        }
    }
}