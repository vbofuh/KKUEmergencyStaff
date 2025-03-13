// พาธ: com.kku.emergencystaff/MainActivity.kt
package com.example.sosstaff

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.sosstaff.auth.LoginActivity
import com.example.sosstaff.main.MainContainer

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    // app/src/main/java/com/example/sosstaff/MainActivity.kt
// Update the onCreate method:

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Check if user is signed in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Check if the user is a staff member
            checkIfUserIsStaff(currentUser.uid)
        } else {
            // No user is signed in
            navigateToLogin()
        }

        // Handle notification intent
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val incidentId = intent.getStringExtra("incidentId")
        val chatId = intent.getStringExtra("chatId")

        // Store these values to pass to MainContainer when launched
        if (incidentId != null || chatId != null) {
            val extras = Bundle()
            if (incidentId != null) extras.putString("incidentId", incidentId)
            if (chatId != null) extras.putString("chatId", chatId)

            // Store for later use
            pendingNavigationExtras = extras
        }
    }

    private fun navigateToMainContainer() {
        val intent = Intent(this, MainContainer::class.java)
        // Pass any stored navigation data
        if (pendingNavigationExtras != null) {
            intent.putExtras(pendingNavigationExtras!!)
        }
        startActivity(intent)
        finish()
    }
}