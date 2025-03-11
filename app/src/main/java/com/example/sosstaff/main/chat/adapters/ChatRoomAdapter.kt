// พาธ: com.kku.emergencystaff/main/chat/adapters/ChatRoomAdapter.kt
package com.kku.emergencystaff.main.chat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.kku.emergencystaff.R
import com.kku.emergencystaff.models.ChatRoom

class ChatRoomAdapter(
    private val onItemClick: (ChatRoom) -> Unit
) : ListAdapter<ChatRoom, ChatRoomAdapter.ChatRoomViewHolder>(ChatRoomDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_room, parent, false)
        return ChatRoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        val chatRoom = getItem(position)
        holder.bind(chatRoom, onItemClick)
    }

    class ChatRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        private val tvIncidentType: TextView = itemView.findViewById(R.id.tvIncidentType)
        private val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        private val tvLastMessageTime: TextView = itemView.findViewById(R.id.tvLastMessageTime)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvUnreadCount: TextView = itemView.findViewById(R.id.tvUnreadCount)

        fun bind(chatRoom: ChatRoom, onItemClick: (ChatRoom) -> Unit) {
            tvUserName.text = chatRoom.userName
            tvIncidentType.text = chatRoom.incidentType
            tvLastMessage.text = chatRoom.lastMessage
            tvLastMessageTime.text = chatRoom.getFormattedLastMessageTime()
            tvStatus.text = chatRoom.getStatusText()

            // กำหนดสีสถานะ
            val statusColor = chatRoom.getStatusColor()
            tvStatus.setTextColor(statusColor)

            // แสดงจำนวนข้อความที่ยังไม่ได้อ่าน
            if (chatRoom.staffUnreadCount > 0) {
                tvUnreadCount.visibility = View.VISIBLE
                tvUnreadCount.text = chatRoom.staffUnreadCount.toString()
            } else {
                tvUnreadCount.visibility = View.GONE
            }

            // กำหนดสีพื้นหลังตามสถานะการใช้งาน
            if (chatRoom.active) {
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.chat_active_bg))
            } else {
                cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.chat_inactive_bg))
            }

            // ตั้งค่าการทำงานเมื่อคลิก
            itemView.setOnClickListener {
                onItemClick(chatRoom)
            }
        }
    }

    // DiffUtil สำหรับการอัปเดตรายการอย่างมีประสิทธิภาพ
    class ChatRoomDiffCallback : DiffUtil.ItemCallback<ChatRoom>() {
        override fun areItemsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
            return oldItem == newItem
        }
    }
}