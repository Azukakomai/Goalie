package com.tasktracker.daily

import android.app.Application
import com.tasktracker.daily.data.AppDatabase

class DailyTrackerApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
}
