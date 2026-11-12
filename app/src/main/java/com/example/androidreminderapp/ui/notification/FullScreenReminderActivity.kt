package com.example.androidreminderapp.ui.notification

import android.app.PendingIntent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.androidreminderapp.service.NotificationHelper
import com.example.androidreminderapp.ui.theme.AndroidReminderAppTheme

class FullScreenReminderActivity : ComponentActivity() {
    private var alert by mutableStateOf<ReminderAlert?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureLockScreen()
        alert = readAlert(intent)
        if (alert == null) {
            Log.e(TAG, "Full-screen reminder activity was launched without valid reminder data.")
            finish()
            return
        }

        setContent {
            AndroidReminderAppTheme {
                alert?.let { currentAlert ->
                    FullScreenReminderScreen(
                        alert = currentAlert,
                        onSnooze = { performAction(currentAlert, NotificationHelper.ACTION_SNOOZE) },
                        onDismiss = { performAction(currentAlert, NotificationHelper.ACTION_DISMISS) },
                        onMarkDone = { performAction(currentAlert, NotificationHelper.ACTION_MARK_DONE) }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        alert = readAlert(intent)
        if (alert == null) {
            Log.e(TAG, "New full-screen reminder intent did not contain reminder data.")
            finish()
        }
    }

    private fun configureLockScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    private fun readAlert(intent: android.content.Intent): ReminderAlert? {
        val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)
        if (reminderId <= 0L) return null
        return ReminderAlert(
            reminderId = reminderId,
            title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: "Reminder",
            description = intent.getStringExtra(NotificationHelper.EXTRA_DESCRIPTION)
                ?: "Time to start your day ✨",
            snoozeMinutes = intent.getIntExtra(NotificationHelper.EXTRA_SNOOZE_MINUTES, 10)
                .coerceAtLeast(1)
        )
    }

    private fun performAction(alert: ReminderAlert, action: String) {
        try {
            NotificationHelper.actionPendingIntent(
                context = this,
                action = action,
                reminderId = alert.reminderId,
                title = alert.title,
                description = alert.description,
                snoozeMinutes = alert.snoozeMinutes
            ).send()
            finish()
        } catch (exception: PendingIntent.CanceledException) {
            Log.e(TAG, "Unable to deliver reminder action $action.", exception)
        }
    }

    companion object {
        private const val TAG = "FullScreenReminder"
    }
}

data class ReminderAlert(
    val reminderId: Long,
    val title: String,
    val description: String,
    val snoozeMinutes: Int
)
