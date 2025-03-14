// พาธ: com.kku.emergencystaff/main/MainContainer.kt
package com.example.sosstaff.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.sosstaff.R
import com.example.sosstaff.auth.LoginActivity
import com.example.sosstaff.auth.repository.AuthRepository
import com.example.sosstaff.common.utils.NotificationUtils
import com.example.sosstaff.main.chat.ChatListFragment
import com.example.sosstaff.main.incidents.IncidentsFragment
import com.example.sosstaff.main.profile.ProfileFragment
import org.koin.android.ext.android.inject

class MainContainer : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private var chatBadge: BadgeDrawable? = null

    // ใช้ Koin เพื่อฉีด authRepository แทนการประกาศแบบเดิม
    private val authRepository: AuthRepository by inject()

    // Don't initialize fragments in class declaration - move to onCreate
    private lateinit var incidentsFragment: IncidentsFragment
    private lateinit var chatListFragment: ChatListFragment
    private lateinit var profileFragment: ProfileFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_container)

        // Initialize fragments here
        incidentsFragment = IncidentsFragment()
        chatListFragment = ChatListFragment()
        profileFragment = ProfileFragment()

        // สร้างช่องทางการแจ้งเตือน
        NotificationUtils.createNotificationChannels(this)

        bottomNavigation = findViewById(R.id.bottomNavigation)
        setupBottomNavigation()

        // ตรวจสอบว่ามีผู้ใช้ล็อกอินอยู่หรือไม่
        checkLoggedInUser()

        // เริ่มต้นที่หน้ารายการเหตุการณ์
        if (savedInstanceState == null) {
            loadFragment(incidentsFragment)
        }

        // ตรวจสอบว่ามีการส่ง Intent มาพร้อมกับการเปิดแอป
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        // ตรวจสอบว่าเปิดแอปจากการแจ้งเตือนหรือไม่
        val incidentId = intent.getStringExtra("EXTRA_INCIDENT_ID")
        val chatId = intent.getStringExtra("EXTRA_CHAT_ID")

        when {
            incidentId != null -> {
                // เปิดรายละเอียดเหตุการณ์
                val detailIntent = Intent(this, IncidentsFragment::class.java).apply {
                    putExtra("EXTRA_INCIDENT_ID", incidentId)
                }
                startActivity(detailIntent)
            }

            chatId != null -> {
                // เปิดห้องแชท
                val chatIntent = Intent(this, ChatListFragment::class.java).apply {
                    putExtra("EXTRA_CHAT_ID", chatId)
                }
                startActivity(chatIntent)
                // เปลี่ยน tab ไปยังแชท
                bottomNavigation.selectedItemId = R.id.navigation_chat
            }
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_incidents -> loadFragment(incidentsFragment)
                R.id.navigation_chat -> loadFragment(chatListFragment)
                R.id.navigation_profile -> loadFragment(profileFragment)
                else -> false
            }
            true
        }

        // สร้าง Badge สำหรับแสดงจำนวนข้อความที่ยังไม่ได้อ่าน
        chatBadge = bottomNavigation.getOrCreateBadge(R.id.navigation_chat)
        chatBadge?.isVisible = false
    }

    fun updateChatBadge(count: Int) {
        if (count > 0) {
            chatBadge?.number = count
            chatBadge?.isVisible = true
        } else {
            chatBadge?.isVisible = false
        }
    }

    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        return true
    }

    private fun checkLoggedInUser() {
        val currentUser = authRepository.getCurrentStaff()
        currentUser.observe(this) { staff ->
            if (staff == null) {
                // ถ้าไม่มีผู้ใช้ล็อกอิน ให้ไปยังหน้าล็อกอิน
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }
}