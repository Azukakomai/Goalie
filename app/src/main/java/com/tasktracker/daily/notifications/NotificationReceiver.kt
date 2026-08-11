package com.tasktracker.daily.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = NotificationPreferences(context)
        if (prefs.isNotificationsEnabled) {
            NotificationHelper.sendGoalReminderNotification(
                context = context,
                title = "Goalie 🎯",
                message = "Have you checked your goals today?"
            )
        }
    }
}
