// พาธ: com.kku.emergencystaff/main/chat/adapters/MessageAdapter.kt
package com.example.sosstaff.main.chat.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sosstaff.R
import com.example.sosstaff.models.Message
import java.util.Calendar

class MessageAdapter(
    private val currentUserId: String
) : ListAdapter<MessageListItem, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_DATE_HEADER = 0
        private const val VIEW_TYPE_MESSAGE_SENT = 1
        private const val VIEW_TYPE_MESSAGE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = getItem(position)) {
            is MessageListItem.DateHeader -> VIEW_TYPE_DATE_HEADER
            is MessageListItem.MessageItem -> {
                if (item.message.isFromCurrentUser(currentUserId)) {
                    VIEW_TYPE_MESSAGE_SENT
                } else {
                    VIEW_TYPE_MESSAGE_RECEIVED
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_DATE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_date_header, parent, false)
                DateHeaderViewHolder(view)
            }
            VIEW_TYPE_MESSAGE_SENT -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_sent, parent, false)
                SentMessageViewHolder(view)
            }
            else -> { // VIEW_TYPE_MESSAGE_RECEIVED
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_message_received, parent, false)
                ReceivedMessageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)

        when (holder) {
            is DateHeaderViewHolder -> {
                if (item is MessageListItem.DateHeader) {
                    holder.bind(item.date)
                }
            }
            is SentMessageViewHolder -> {
                if (item is MessageListItem.MessageItem) {
                    holder.bind(item.message)
                }
            }
            is ReceivedMessageViewHolder -> {
                if (item is MessageListItem.MessageItem) {
                    holder.bind(item.message)
                }
            }
        }
    }

    // แปลงรายการข้อความให้รวมหัวข้อวันที่
    fun submitMessageList(messages: List<Message>) {
        val messageListItems = mutableListOf<MessageListItem>()
        var currentDate = ""

        messages.forEach { message ->
            val messageDate = message.getFormattedDate()

            // ถ้าวันที่เปลี่ยน ให้เพิ่มหัวข้อวันที่
            if (messageDate != currentDate) {
                messageListItems.add(MessageListItem.DateHeader(messageDate))
                currentDate = messageDate
            }

            // เพิ่มข้อความ
            messageListItems.add(MessageListItem.MessageItem(message))
        }

        submitList(messageListItems)
    }

    // ViewHolder สำหรับหัวข้อวันที่
    class DateHeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        fun bind(date: String) {
            tvDate.text = date
        }
    }

    // ViewHolder สำหรับข้อความที่ส่งโดยเจ้าหน้าที่
    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.tvMessage)
        private val timeText: TextView = itemView.findViewById(R.id.tvTime)

        fun bind(message: Message) {
            messageText.text = message.message
            timeText.text = message.getFormattedTime()
        }
    }

    // ViewHolder สำหรับข้อความที่รับจากผู้ใช้
    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.tvMessage)
        private val timeText: TextView = itemView.findViewById(R.id.tvTime)
        private val nameText: TextView = itemView.findViewById(R.id.tvSenderName)

        fun bind(message: Message) {
            messageText.text = message.message
            timeText.text = message.getFormattedTime()
            nameText.text = message.senderName
        }
    }

    // DiffUtil สำหรับการอัปเดตรายการอย่างมีประสิทธิภาพ
    class MessageDiffCallback : DiffUtil.ItemCallback<MessageListItem>() {
        override fun areItemsTheSame(oldItem: MessageListItem, newItem: MessageListItem): Boolean {
            return when {
                oldItem is MessageListItem.DateHeader && newItem is MessageListItem.DateHeader ->
                    oldItem.date == newItem.date
                oldItem is MessageListItem.MessageItem && newItem is MessageListItem.MessageItem ->
                    oldItem.message.id == newItem.message.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: MessageListItem, newItem: MessageListItem): Boolean {
            return oldItem == newItem
        }
    }
}

// Sealed class สำหรับรายการในรายการข้อความ
sealed class MessageListItem {
    data class DateHeader(val date: String) : MessageListItem()
    data class MessageItem(val message: Message) : MessageListItem()
}