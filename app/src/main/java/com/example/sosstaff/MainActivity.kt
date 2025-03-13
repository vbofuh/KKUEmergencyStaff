package com.example.sosstaff

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.sosstaff.auth.LoginActivity
import com.example.sosstaff.main.MainContainer

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var pendingNavigationExtras: Bundle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply splash screen if available
        try {
            val splashScreen = installSplashScreen()
            splashScreen.setKeepOnScreenCondition { true }
        } catch (e: Exception) {
            Log.e(TAG, "Splash screen error: ${e.message}")
        }

        super.onCreate(savedInstanceState)

        // Initialize Firebase services
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Handle intent if the app was opened from a notification
        handleIntent(intent)

        // Check if user is already signed in
        checkAuthenticationStatus()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle new intents (e.g., from notifications when app is running)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val incidentId = intent.getStringExtra("EXTRA_INCIDENT_ID")
        val chatId = intent.getStringExtra("EXTRA_CHAT_ID")

        // Store navigation extras to pass to MainContainer when launched
        if (incidentId != null || chatId != null) {
            Log.d(TAG, "Intent extras - incidentId: $incidentId, chatId: $chatId")

            pendingNavigationExtras = Bundle().apply {
                if (incidentId != null) putString("EXTRA_INCIDENT_ID", incidentId)
                if (chatId != null) putString("EXTRA_CHAT_ID", chatId)
            }
        }
    }

    private fun checkAuthenticationStatus() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is signed in, check if they are a staff member
            checkIfUserIsStaff(currentUser.uid)
        } else {
            // No user is signed in
            navigateToLogin()
        }
    }

    private fun checkIfUserIsStaff(userId: String) {
        // Check the 'staff' collection for this user
        firestore.collection("staff")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // User is a staff member, navigate to main container
                    Log.d(TAG, "User is staff member: $userId")
                    navigateToMainContainer()
                } else {
                    // User is not a staff member
                    Log.d(TAG, "User is not a staff member: $userId")
                    auth.signOut() // Sign out the non-staff user
                    navigateToLogin()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error checking staff status: ${e.message}")
                // Error occurred, navigate to login for safety
                auth.signOut()
                navigateToLogin()
            }
    }

    private fun navigateToMainContainer() {
        val intent = Intent(this, MainContainer::class.java).apply {
            // Add any notification data if present
            pendingNavigationExtras?.let {
                putExtras(it)
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}