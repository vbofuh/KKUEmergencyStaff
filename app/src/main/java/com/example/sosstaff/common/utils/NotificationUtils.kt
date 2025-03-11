// พาธ: com.kku.emergencystaff/common/utils/NotificationUtils.kt
package com.example.sosstaff.common.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.sosstaff.R
import com.kku.emergencystaff.main.MainContainer
import com.kku.emergencystaff.main.chat.ChatActivity
import com.kku.emergencystaff.main.incidents.IncidentDetailActivity

object NotificationUtils {

    private const val CHANNEL_ID_EMERGENCY = "channel_emergency"
    private const val CHANNEL_ID_CHAT = "channel_chat"

    private const val NOTIFICATION_ID_EMERGENCY = 1001
    private const val NOTIFICATION_ID_CHAT = 1002

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // สร้างช่องทางการแจ้งเตือนเหตุฉุกเฉิน
            val emergencyChannel = NotificationChannel(
                CHANNEL_ID_EMERGENCY,
                "เหตุฉุกเฉิน",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "การแจ้งเตือนสำหรับเหตุฉุกเฉินใหม่"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
            }

            // สร้างช่องทางการแจ้งเตือนแชท
            val chatChannel = NotificationChannel(
                CHANNEL_ID_CHAT,
                "ข้อความแชท",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "การแจ้งเตือนสำหรับข้อความแชทใหม่"
            }

            notificationManager.createNotificationChannel(emergencyChannel)
            notificationManager.createNotificationChannel(chatChannel)
        }
    }

    fun showEmergencyNotification(context: Context, incidentId: String, title: String, message: String) {
        val intent = Intent(context, IncidentDetailActivity::class.java).apply {
            putExtra("EXTRA_INCIDENT_ID", incidentId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID_EMERGENCY)
            .setSmallIcon(R.drawable.ic_notification_emergency)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_EMERGENCY, notificationBuilder.build())
    }

    fun showChatNotification(context: Context, chatId: String, senderName: String, message: String) {
        val intent = Intent(context, ChatActivity::class.java).apply {
            putExtra("EXTRA_CHAT_ID", chatId)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID_CHAT)
            .setSmallIcon(R.drawable.ic_notification_chat)
            .setContentTitle(senderName)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_CHAT, notificationBuilder.build())
    }

    fun cancelAllNotifications(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }
}