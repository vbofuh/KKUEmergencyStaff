// พาธ: com.kku.emergencystaff/services/FirebaseMessagingService.kt
package com.example.sosstaff.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.sosstaff.R
import com.example.sosstaff.common.utils.NotificationUtils
import com.example.sosstaff.main.MainContainer
import com.example.sosstaff.main.chat.ChatActivity
import com.example.sosstaff.main.incidents.IncidentDetailActivity

class AppFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // ตรวจสอบว่ามีข้อมูลหรือไม่
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            val type = remoteMessage.data["type"]
            val title = remoteMessage.data["title"] ?: ""
            val message = remoteMessage.data["message"] ?: ""

            when (type) {
                "new_incident" -> {
                    val incidentId = remoteMessage.data["incidentId"] ?: ""
                    if (incidentId.isNotEmpty()) {
                        NotificationUtils.showEmergencyNotification(
                            this,
                            incidentId,
                            title,
                            message
                        )
                    }
                }
                "new_message" -> {
                    val chatId = remoteMessage.data["chatId"] ?: ""
                    val senderName = remoteMessage.data["senderName"] ?: ""
                    if (chatId.isNotEmpty()) {
                        NotificationUtils.showChatNotification(
                            this,
                            chatId,
                            senderName,
                            message
                        )
                    }
                }
                "status_update" -> {
                    val incidentId = remoteMessage.data["incidentId"] ?: ""
                    if (incidentId.isNotEmpty()) {
                        showNotification(title, message, incidentId, NotificationType.INCIDENT)
                    }
                }
                else -> {
                    // กรณีการแจ้งเตือนทั่วไป
                    showNotification(title, message)
                }
            }
        }

        // ตรวจสอบว่ามีการแจ้งเตือนหรือไม่
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            showNotification(it.title, it.body)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // ถ้ามีการล็อกอินอยู่ ให้อัปเดต FCM Token
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            updateTokenInFirestore(userId, token)
        }
    }

    private fun updateTokenInFirestore(userId: String, token: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("staff")
            .document(userId)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.d(TAG, "FCM Token updated successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to update FCM Token: ${e.message}")
            }
    }

    private fun showNotification(
        title: String?,
        message: String?,
        id: String = "",
        type: NotificationType = NotificationType.GENERAL
    ) {
        val intent = when (type) {
            NotificationType.INCIDENT -> Intent(this, IncidentDetailActivity::class.java).apply {
                putExtra(IncidentDetailActivity.EXTRA_INCIDENT_ID, id)
            }
            NotificationType.CHAT -> Intent(this, ChatActivity::class.java).apply {
                putExtra(ChatActivity.EXTRA_CHAT_ID, id)
            }
            else -> Intent(this, MainContainer::class.java)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = when (type) {
            NotificationType.INCIDENT -> "emergency_channel"
            NotificationType.CHAT -> "chat_channel"
            else -> "general_channel"
        }

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android Oreo หรือใหม่กว่าต้องมีการสร้าง notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = when (type) {
                NotificationType.INCIDENT -> NotificationChannel(
                    channelId,
                    "Emergency Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                )
                NotificationType.CHAT -> NotificationChannel(
                    channelId,
                    "Chat Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                else -> NotificationChannel(
                    channelId,
                    "General Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = when (type) {
            NotificationType.INCIDENT -> 1
            NotificationType.CHAT -> 2
            else -> 0
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private enum class NotificationType {
        GENERAL,
        INCIDENT,
        CHAT
    }
}