package com.example.hobbyhub

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class HobbyHubApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "event_reminders"
            val channelName = "Event Reminders"
            val channelDescription = "Notifications for scheduled events"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }
}
