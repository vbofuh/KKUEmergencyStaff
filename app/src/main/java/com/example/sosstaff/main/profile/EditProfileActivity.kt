// พาธ: com.kku.emergencystaff/main/profile/EditProfileActivity.kt
package com.example.sosstaff.main.profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sosstaff.databinding.ActivityEditProfileBinding
import com.example.sosstaff.main.profile.viewmodels.ProfileViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val viewModel: ProfileViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // ตั้งค่าปุ่มย้อนกลับ
        binding.btnBack.setOnClickListener {
            finish()
        }

        // ตั้งค่าปุ่มบันทึก
        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()

            if (validateInputs(name, phone)) {
                viewModel.updateProfile(name, phone)
            }
        }
    }

    private fun observeViewModel() {
        // สังเกตการณ์ข้อมูลโปรไฟล์เจ้าหน้าที่
        viewModel.staffProfile.observe(this) { staffUser ->
            if (staffUser != null) {
                // กำหนดค่าเริ่มต้นในฟอร์ม
                binding.etName.setText(staffUser.name)
                binding.etPhone.setText(staffUser.phone)
                binding.tvEmail.text = staffUser.email
                binding.tvPosition.text = staffUser.position
            }
        }

        // สังเกตการณ์ผลลัพธ์การอัปเดตโปรไฟล์
        viewModel.updateProfileResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "อัปเดตข้อมูลสำเร็จ", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "ไม่สามารถอัปเดตข้อมูลได้", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInputs(name: String, phone: String): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            binding.etName.error = "กรุณากรอกชื่อ"
            isValid = false
        }

        if (phone.isEmpty()) {
            binding.etPhone.error = "กรุณากรอกเบอร์โทรศัพท์"
            isValid = false
        }

        return isValid
    }
}