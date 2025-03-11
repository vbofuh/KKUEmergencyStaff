// พาธ: com.kku.emergencystaff/main/chat/ChatListFragment.kt
package com.kku.emergencystaff.main.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.kku.emergencystaff.databinding.FragmentChatListBinding
import com.kku.emergencystaff.main.MainContainer
import com.kku.emergencystaff.main.chat.adapters.ChatRoomAdapter
import com.kku.emergencystaff.main.chat.viewmodels.ChatViewModel
import com.kku.emergencystaff.models.ChatRoom
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatListFragment : Fragment() {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var chatRoomAdapter: ChatRoomAdapter
    private lateinit var unassignedChatRoomAdapter: ChatRoomAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupTabs()
        observeViewModel()

        // ตรวจสอบว่ามี intent ส่งมาจากการแจ้งเตือนหรือไม่
        val chatId = arguments?.getString("EXTRA_CHAT_ID")
        if (chatId != null) {
            onChatRoomSelected(chatId)
        }
    }

    private fun setupRecyclerViews() {
        // ตั้งค่า RecyclerView สำหรับห้องแชทที่รับผิดชอบ
        chatRoomAdapter = ChatRoomAdapter { chatRoom ->
            onChatRoomSelected(chatRoom.id)
        }
        binding.rvChatRooms.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatRoomAdapter
        }

        // ตั้งค่า RecyclerView สำหรับห้องแชทที่ยังไม่มีเจ้าหน้าที่รับผิดชอบ
        unassignedChatRoomAdapter = ChatRoomAdapter { chatRoom ->
            showAssignChatRoomDialog(chatRoom)
        }
        binding.rvUnassignedChatRooms.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = unassignedChatRoomAdapter
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        // แสดงห้องแชทที่รับผิดชอบทั้งหมด
                        viewModel.setChatFilter(ChatViewModel.ChatFilter.ALL)
                        binding.rvChatRooms.visibility = View.VISIBLE
                        binding.rvUnassignedChatRooms.visibility = View.GONE
                        binding.tvUnassignedHeader.visibility = View.GONE
                    }
                    1 -> {
                        // แสดงห้องแชทที่กำลังใช้งาน
                        viewModel.setChatFilter(ChatViewModel.ChatFilter.ACTIVE)
                        binding.rvChatRooms.visibility = View.VISIBLE
                        binding.rvUnassignedChatRooms.visibility = View.GONE
                        binding.tvUnassignedHeader.visibility = View.GONE
                    }
                    2 -> {
                        // แสดงห้องแชทที่เสร็จสิ้นแล้ว
                        viewModel.setChatFilter(ChatViewModel.ChatFilter.COMPLETED)
                        binding.rvChatRooms.visibility = View.VISIBLE
                        binding.rvUnassignedChatRooms.visibility = View.GONE
                        binding.tvUnassignedHeader.visibility = View.GONE
                    }
                    3 -> {
                        // แสดงห้องแชทที่ยังไม่มีเจ้าหน้าที่รับผิดชอบ
                        binding.rvChatRooms.visibility = View.GONE
                        binding.rvUnassignedChatRooms.visibility = View.VISIBLE
                        binding.tvUnassignedHeader.visibility = View.VISIBLE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // เริ่มต้นที่แท็บห้องแชททั้งหมด
        binding.tabLayout.getTabAt(0)?.select()
    }

    private fun observeViewModel() {
        // สังเกตการณ์ห้องแชทที่กรองแล้ว
        viewModel.filteredChatRooms.observe(viewLifecycleOwner) { chatRooms ->
            chatRoomAdapter.submitList(chatRooms)
            updateEmptyView(chatRooms.isEmpty())

            // อัปเดตแบดจ์จำนวนข้อความที่ยังไม่ได้อ่าน
            updateUnreadBadge()
        }

        // สังเกตการณ์ห้องแชทที่ยังไม่มีเจ้าหน้าที่รับผิดชอบ
        viewModel.unassignedChatRooms.observe(viewLifecycleOwner) { chatRooms ->
            unassignedChatRoomAdapter.submitList(chatRooms)
            updateUnassignedEmptyView(chatRooms.isEmpty())

            // อัปเดตแบดจ์จำนวนห้องแชทที่ยังไม่มีเจ้าหน้าที่รับผิดชอบ
            updateUnassignedBadge(chatRooms.size)
        }
    }

    private fun updateEmptyView(isEmpty: Boolean) {
        binding.tvEmptyChatRooms.visibility = if (isEmpty && binding.rvChatRooms.visibility == View.VISIBLE) View.VISIBLE else View.GONE
    }

    private fun updateUnassignedEmptyView(isEmpty: Boolean) {
        binding.tvEmptyUnassigned.visibility = if (isEmpty && binding.rvUnassignedChatRooms.visibility == View.VISIBLE) View.VISIBLE else View.GONE
    }

    private fun updateUnreadBadge() {
        // อัปเดตแบดจ์ในแถบนำทางด้านล่าง
        (requireActivity() as? MainContainer)?.updateChatBadge(viewModel.totalUnreadCount.value ?: 0)
    }

    private fun updateUnassignedBadge(count: Int) {
        // อัปเดตแบดจ์ในแท็บห้องแชทที่ยังไม่มีเจ้าหน้าที่
        val unassignedTab = binding.tabLayout.getTabAt(3)
        if (count > 0) {
            unassignedTab?.orCreateBadge?.apply {
                number = count
                isVisible = true
            }
        } else {
            unassignedTab?.removeBadge()
        }
    }

    private fun onChatRoomSelected(chatId: String) {
        val intent = Intent(requireContext(), ChatActivity::class.java).apply {
            putExtra(ChatActivity.EXTRA_CHAT_ID, chatId)
        }
        startActivity(intent)
    }

    private fun showAssignChatRoomDialog(chatRoom: ChatRoom) {
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("รับเรื่องใหม่")
            .setMessage("คุณต้องการรับเรื่อง \"${chatRoom.incidentType}\" จากคุณ ${chatRoom.userName} ใช่หรือไม่?")
            .setPositiveButton("รับเรื่อง") { _, _ ->
                assignChatRoom(chatRoom.id)
            }
            .setNegativeButton("ยกเลิก", null)
            .create()

        dialog.show()
    }

    private fun assignChatRoom(chatId: String) {
        viewModel.assignChatRoom(chatId).observe(viewLifecycleOwner) { success ->
            if (success) {
                // เปิดห้องแชทที่รับเรื่องใหม่
                onChatRoomSelected(chatId)
            } else {
                com.google.android.material.snackbar.Snackbar.make(
                    binding.root,
                    "ไม่สามารถรับเรื่องได้",
                    com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}