package com.example.androidreminderapp

import android.app.Application
import com.example.androidreminderapp.service.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AndroidReminderApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannel(this)
    }
}
