// พาธ: com.kku.emergencystaff/main/chat/ChatListFragment.kt
package com.example.sosstaff.main.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.example.sosstaff.databinding.FragmentChatListBinding
import com.example.sosstaff.main.MainContainer
import com.example.sosstaff.main.chat.adapters.ChatRoomAdapter
import com.example.sosstaff.main.chat.viewmodels.ChatViewModel
import com.example.sosstaff.models.ChatRoom
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel


class ChatListFragment : Fragment() {

    private var _binding: FragmentChatListBinding? = null
    private val binding get() = _binding!!

    // ใช้ Koin เพื่อฉีด viewModel
    private val viewModel: ChatViewModel by viewModel()
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
        binding.tvEmptyChatRooms.visibility =
            if (isEmpty && binding.rvChatRooms.visibility == View.VISIBLE) View.VISIBLE else View.GONE
    }

    private fun updateUnassignedEmptyView(isEmpty: Boolean) {
        binding.tvEmptyUnassigned.visibility =
            if (isEmpty && binding.rvUnassignedChatRooms.visibility == View.VISIBLE) View.VISIBLE else View.GONE
    }

    private fun updateUnreadBadge() {
        // อัปเดตแบดจ์ในแถบนำทางด้านล่าง
        (requireActivity() as? MainContainer)?.updateChatBadge(
            viewModel.totalUnreadCount.value ?: 0
        )
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
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("รับผิดชอบห้องแชทนี้")
            .setMessage("คุณต้องการรับผิดชอบการสนทนากับ ${chatRoom.userName} เกี่ยวกับ \"${chatRoom.incidentType}\" ใช่หรือไม่?")
            .setPositiveButton("รับผิดชอบ") { _, _ ->
                viewModel.assignChatRoom(chatRoom.id).observe(viewLifecycleOwner) { success ->
                    if (success) {
                        Toast.makeText(
                            requireContext(),
                            "รับผิดชอบห้องแชทสำเร็จ",
                            Toast.LENGTH_SHORT
                        ).show()
                        onChatRoomSelected(chatRoom.id)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "ไม่สามารถรับผิดชอบห้องแชทได้",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }
}