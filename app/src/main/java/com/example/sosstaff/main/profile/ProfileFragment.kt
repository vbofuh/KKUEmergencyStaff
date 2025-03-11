// พาธ: com.kku.emergencystaff/main/profile/ProfileFragment.kt
package com.example.sosstaff.main.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.sosstaff.R
import com.example.sosstaff.auth.LoginActivity
import com.example.sosstaff.databinding.FragmentProfileBinding
import com.example.sosstaff.main.profile.viewmodels.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // ตั้งค่าปุ่มแก้ไขโปรไฟล์
        binding.btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        // ตั้งค่าปุ่มเปลี่ยนรหัสผ่าน
        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        // ตั้งค่าปุ่มเปลี่ยนสถานะ
        binding.btnChangeStatus.setOnClickListener {
            showChangeStatusDialog()
        }

        // ตั้งค่าปุ่มออกจากระบบ
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun observeViewModel() {
        // สังเกตการณ์ข้อมูลโปรไฟล์เจ้าหน้าที่
        viewModel.staffProfile.observe(viewLifecycleOwner) { staffUser ->
            if (staffUser != null) {
                // อัปเดตข้อมูลในหน้า
                binding.tvName.text = staffUser.name
                binding.tvEmail.text = staffUser.email
                binding.tvPhone.text = staffUser.phone
                binding.tvPosition.text = staffUser.position
                binding.tvStatus.text = staffUser.getStatusText()

                // กำหนดสีสถานะ
                val statusColor = staffUser.getStatusColor()
                binding.tvStatus.setTextColor(statusColor)
            }
        }

        // สังเกตการณ์ผลลัพธ์การอัปเดตโปรไฟล์
        viewModel.updateProfileResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "อัปเดตข้อมูลสำเร็จ", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "ไม่สามารถอัปเดตข้อมูลได้", Toast.LENGTH_SHORT).show()
            }
        }

        // สังเกตการณ์ผลลัพธ์การเปลี่ยนรหัสผ่าน
        viewModel.changePasswordResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "เปลี่ยนรหัสผ่านสำเร็จ", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "ไม่สามารถเปลี่ยนรหัสผ่านได้", Toast.LENGTH_SHORT).show()
            }
        }

        // สังเกตการณ์ผลลัพธ์การออกจากระบบ
        viewModel.logoutResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                navigateToLogin()
            } else {
                Toast.makeText(requireContext(), "ไม่สามารถออกจากระบบได้", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditProfileDialog() {
        // สร้าง DialogView จากเลย์เอาต์
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_profile, null)

        // ดึงข้อมูลโปรไฟล์ปัจจุบัน
        val currentProfile = viewModel.staffProfile.value

        // ผูกตัวแปรกับ views ใน dialog
        val etName = dialogView.findViewById<android.widget.EditText>(R.id.etName)
        val etPhone = dialogView.findViewById<android.widget.EditText>(R.id.etPhone)

        // กำหนดค่าเริ่มต้น
        etName.setText(currentProfile?.name)
        etPhone.setText(currentProfile?.phone)

        // สร้างและแสดง dialog
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("แก้ไขข้อมูลส่วนตัว")
            .setView(dialogView)
            .setPositiveButton("บันทึก") { _, _ ->
                val name = etName.text.toString().trim()
                val phone = etPhone.text.toString().trim()

                if (name.isNotEmpty() && phone.isNotEmpty()) {
                    viewModel.updateProfile(name, phone)
                } else {
                    Toast.makeText(requireContext(), "กรุณากรอกข้อมูลให้ครบถ้วน", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    private fun showChangePasswordDialog() {
        // สร้าง DialogView จากเลย์เอาต์
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_change_password, null)

        // ผูกตัวแปรกับ views ใน dialog
        val etNewPassword = dialogView.findViewById<android.widget.EditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<android.widget.EditText>(R.id.etConfirmPassword)

        // สร้างและแสดง dialog
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("เปลี่ยนรหัสผ่าน")
            .setView(dialogView)
            .setPositiveButton("บันทึก") { _, _ ->
                val newPassword = etNewPassword.text.toString()
                val confirmPassword = etConfirmPassword.text.toString()

                if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(requireContext(), "กรุณากรอกข้อมูลให้ครบถ้วน", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword != confirmPassword) {
                    Toast.makeText(requireContext(), "รหัสผ่านไม่ตรงกัน", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (!viewModel.isValidPassword(newPassword)) {
                    Toast.makeText(
                        requireContext(),
                        "รหัสผ่านต้องมีความยาวอย่างน้อย 8 ตัวและมีตัวอักษรตัวใหญ่ ตัวเล็ก และตัวเลข",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setPositiveButton
                }

                viewModel.changePassword(newPassword, confirmPassword)
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    private fun showChangeStatusDialog() {
        // ตัวเลือกสถานะ
        val statusOptions = arrayOf("ว่าง", "กำลังทำงาน")
        val currentStatus = viewModel.staffProfile.value?.status ?: "ว่าง"
        val currentIndex = statusOptions.indexOf(currentStatus)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("เปลี่ยนสถานะ")
            .setSingleChoiceItems(statusOptions, currentIndex) { dialog, which ->
                val selectedStatus = statusOptions[which]

                viewModel.changeStatus(selectedStatus).observe(viewLifecycleOwner) { success ->
                    if (success) {
                        Toast.makeText(requireContext(), "เปลี่ยนสถานะเป็น \"$selectedStatus\" สำเร็จ", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "ไม่สามารถเปลี่ยนสถานะได้", Toast.LENGTH_SHORT).show()
                    }
                }

                dialog.dismiss()
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("ออกจากระบบ")
            .setMessage("คุณต้องการออกจากระบบใช่หรือไม่?")
            .setPositiveButton("ออกจากระบบ") { _, _ ->
                viewModel.logout()
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}