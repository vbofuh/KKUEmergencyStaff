package com.example.sosstaff.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // จัดการกับข้อความที่ได้รับที่นี่
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // อัปเดตโทเค็นใหม่ในฐานข้อมูลที่นี่
    }
}