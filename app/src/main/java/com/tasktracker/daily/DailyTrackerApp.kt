package com.tasktracker.daily

import android.app.Application
import com.tasktracker.daily.data.AppDatabase
import com.tasktracker.daily.notifications.NotificationHelper

class DailyTrackerApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
