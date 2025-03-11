// พาธ: com.kku.emergencystaff/main/incidents/adapters/IncidentAdapter.kt
package com.example.sosstaff.main.incidents.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sosstaff.R
import com.example.sosstaff.common.utils.DateUtils
import com.example.sosstaff.models.Incident

class IncidentAdapter(
    private val onItemClick: (Incident) -> Unit
) : ListAdapter<Incident, IncidentAdapter.IncidentViewHolder>(IncidentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncidentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_incident, parent, false)
        return IncidentViewHolder(view)
    }

    override fun onBindViewHolder(holder: IncidentViewHolder, position: Int) {
        val incident = getItem(position)
        holder.bind(incident, onItemClick)
    }

    class IncidentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardView)
        private val tvIncidentType: TextView = itemView.findViewById(R.id.tvIncidentType)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvReporterName: TextView = itemView.findViewById(R.id.tvReporterName)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)

        fun bind(incident: Incident, onItemClick: (Incident) -> Unit) {
            // กำหนดข้อมูลให้กับ ViewHolder
            tvIncidentType.text = incident.incidentType
            tvLocation.text = incident.location
            tvStatus.text = incident.status
            tvReporterName.text = incident.reporterName

            // กำหนดสีสถานะ
            val statusColor = incident.getStatusColor()
            tvStatus.setTextColor(statusColor)

            // กำหนดวันและเวลา
            if (incident.status == "เสร็จสิ้น" && incident.completedAt != null) {
                // แสดงวันที่เสร็จสิ้น
                tvDate.text = DateUtils.formatDate(incident.completedAt.time)
                tvTime.text = DateUtils.formatTime(incident.completedAt.time)
            } else {
                // แสดงวันที่รายงาน
                tvDate.text = DateUtils.formatDate(incident.reportedAt.time)
                tvTime.text = DateUtils.formatTime(incident.reportedAt.time)
            }

            // ตั้งค่าการทำงานเมื่อคลิก
            itemView.setOnClickListener {
                onItemClick(incident)
            }

            // กำหนดสีพื้นหลังตามสถานะ
            when (incident.status) {
                "รอรับเรื่อง" -> {
                    cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.incident_waiting_bg))
                }
                "เจ้าหน้าที่รับเรื่องแล้ว" -> {
                    cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.incident_assigned_bg))
                }
                "กำลังดำเนินการ" -> {
                    cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.incident_in_progress_bg))
                }
                "เสร็จสิ้น" -> {
                    cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.incident_completed_bg))
                }
            }
        }
    }

    // DiffUtil สำหรับการอัปเดตรายการอย่างมีประสิทธิภาพ
    class IncidentDiffCallback : DiffUtil.ItemCallback<Incident>() {
        override fun areItemsTheSame(oldItem: Incident, newItem: Incident): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Incident, newItem: Incident): Boolean {
            return oldItem == newItem
        }
    }
}