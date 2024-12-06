package com.example.hobbyhub.utility

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.hobbyhub.R
import com.example.hobbyhub.scheduling.view.ui.CreateEventFragment

class EventReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventTitle = intent.getStringExtra("eventTitle") ?: "Event Reminder"
        val eventTime = intent.getStringExtra("eventTime") ?: ""

        val openAppIntent = Intent(context, CreateEventFragment::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "event_reminders")
            .setSmallIcon(R.drawable.ic_logo) // Replace with your icon
            .setContentTitle(eventTitle)
            .setContentText("Scheduled at $eventTime")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(eventTitle.hashCode(), notification)
    }
}