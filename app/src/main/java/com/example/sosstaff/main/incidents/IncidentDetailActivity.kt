// พาธ: com.kku.emergencystaff/main/incidents/IncidentDetailActivity.kt
package com.example.sosstaff.main.incidents

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.sosstaff.R
import com.example.sosstaff.common.utils.DateUtils
import com.example.sosstaff.databinding.ActivityIncidentDetailBinding
import com.example.sosstaff.main.chat.ChatActivity
import com.example.sosstaff.main.incidents.viewmodels.IncidentsViewModel
import com.example.sosstaff.models.Incident
import org.koin.androidx.viewmodel.ext.android.viewModel

class IncidentDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_INCIDENT_ID = "extra_incident_id"
    }

    private lateinit var binding: ActivityIncidentDetailBinding
    private val viewModel: IncidentsViewModel by viewModel()

    private var currentIncident: Incident? = null
    private var incidentId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIncidentDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // รับ ID ของเหตุการณ์จาก Intent
        incidentId = intent.getStringExtra(EXTRA_INCIDENT_ID) ?: ""
        if (incidentId.isEmpty()) {
            Toast.makeText(this, "ไม่พบข้อมูลเหตุการณ์", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        setupStatusSpinner()
        observeViewModel()
    }

    private fun setupUI() {
        // ตั้งค่าปุ่มย้อนกลับ
        binding.btnBack.setOnClickListener {
            finish()
        }

        // ตั้งค่าปุ่มแชท
        binding.btnChat.setOnClickListener {
            if (currentIncident != null) {
                val intent = Intent(this, ChatActivity::class.java).apply {
                    putExtra(ChatActivity.EXTRA_CHAT_ID, incidentId)
                    putExtra(ChatActivity.EXTRA_INCIDENT_TYPE, currentIncident?.incidentType)
                }
                startActivity(intent)
            }
        }

        // ตั้งค่าปุ่มโทร
        binding.btnCall.setOnClickListener {
            if (currentIncident != null && currentIncident?.reporterPhone?.isNotEmpty() == true) {
                val phoneNumber = currentIncident?.reporterPhone
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phoneNumber")
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "ไม่พบเบอร์โทรศัพท์", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupStatusSpinner() {
        val statusOptions = arrayOf(
            "รอรับเรื่อง",
            "เจ้าหน้าที่รับเรื่องแล้ว",
            "กำลังดำเนินการ",
            "เสร็จสิ้น"
        )

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statusOptions)
        binding.statusSpinner.adapter = adapter

        binding.statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedStatus = statusOptions[position]
                if (currentIncident != null && currentIncident?.status != selectedStatus) {
                    confirmStatusChange(selectedStatus)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun confirmStatusChange(newStatus: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("เปลี่ยนสถานะ")
            .setMessage("คุณต้องการเปลี่ยนสถานะเหตุการณ์เป็น \"$newStatus\" ใช่หรือไม่?")
            .setPositiveButton("ยืนยัน") { _, _ ->
                updateIncidentStatus(newStatus)
            }
            .setNegativeButton("ยกเลิก") { _, _ ->
                // ย้อนกลับไปที่สถานะปัจจุบัน
                val currentStatus = currentIncident?.status ?: "รอรับเรื่อง"
                val currentPosition = getStatusPosition(currentStatus)
                binding.statusSpinner.setSelection(currentPosition)
            }
            .show()
    }

    private fun updateIncidentStatus(newStatus: String) {
        viewModel.updateIncidentStatus(incidentId, newStatus).observe(this) { success ->
            if (success) {
                Toast.makeText(this, "อัปเดตสถานะสำเร็จ", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "ไม่สามารถอัปเดตสถานะได้", Toast.LENGTH_SHORT).show()
                // ย้อนกลับไปที่สถานะปัจจุบัน
                val currentStatus = currentIncident?.status ?: "รอรับเรื่อง"
                val currentPosition = getStatusPosition(currentStatus)
                binding.statusSpinner.setSelection(currentPosition)
            }
        }
    }

    private fun getStatusPosition(status: String): Int {
        return when (status) {
            "รอรับเรื่อง" -> 0
            "เจ้าหน้าที่รับเรื่องแล้ว" -> 1
            "กำลังดำเนินการ" -> 2
            "เสร็จสิ้น" -> 3
            else -> 0
        }
    }

    private fun observeViewModel() {
        viewModel.getIncidentById(incidentId).observe(this) { incident ->
            if (incident != null) {
                currentIncident = incident
                updateUI(incident)
            } else {
                Toast.makeText(this, "ไม่พบข้อมูลเหตุการณ์", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun updateUI(incident: Incident) {
        // กำหนดข้อมูลพื้นฐาน
        binding.tvIncidentType.text = incident.incidentType
        binding.tvReporterName.text = incident.reporterName
        binding.tvReporterPhone.text = incident.reporterPhone
        binding.tvLocation.text = incident.location
        binding.tvRelationToVictim.text = incident.relationToVictim
        binding.tvAdditionalInfo.text = incident.additionalInfo
        binding.tvReportedAt.text = DateUtils.formatDateTime(incident.reportedAt.time)

        // ตั้งค่าสถานะปัจจุบัน
        val statusPosition = getStatusPosition(incident.status)
        binding.statusSpinner.setSelection(statusPosition)

        // แสดงข้อมูลเพิ่มเติมตามสถานะ
        if (incident.assignedStaffId.isNotEmpty()) {
            binding.tvAssignedStaff.text = incident.assignedStaffName
            binding.assignedStaffLayout.visibility = View.VISIBLE
        } else {
            binding.assignedStaffLayout.visibility = View.GONE
        }

        if (incident.status == "เสร็จสิ้น" && incident.completedAt != null) {
            binding.tvCompletedAt.text = DateUtils.formatDateTime(incident.completedAt.time)
            binding.completedAtLayout.visibility = View.VISIBLE

            // คำนวณระยะเวลาดำเนินการ
            val duration = DateUtils.calculateDuration(
                incident.reportedAt.time,
                incident.completedAt.time
            )
            binding.tvDuration.text = duration
            binding.durationLayout.visibility = View.VISIBLE
        } else {
            binding.completedAtLayout.visibility = View.GONE
            binding.durationLayout.visibility = View.GONE
        }

        // ตั้งค่าปุ่มแชทและโทรตามสถานะ
        binding.btnChat.isEnabled = incident.isActive()
        binding.btnCall.isEnabled = incident.reporterPhone.isNotEmpty()
    }
}