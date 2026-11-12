package com.example.androidreminderapp.service

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.androidreminderapp.R
import com.example.androidreminderapp.receiver.ReminderNotificationActionReceiver
import com.example.androidreminderapp.ui.notification.FullScreenReminderActivity

object NotificationHelper {
    const val CHANNEL_ID = "notify_reminder_alarms_v2"
    const val NOTIFICATION_ID_BASE = 1000

    const val EXTRA_REMINDER_ID = "notification_reminder_id"
    const val EXTRA_TITLE = "notification_title"
    const val EXTRA_DESCRIPTION = "notification_description"
    const val EXTRA_SNOOZE_MINUTES = "notification_snooze_minutes"

    const val ACTION_OPEN_ALERT = "com.mynominee.notify.action.OPEN_ALERT"
    const val ACTION_SNOOZE = "com.mynominee.notify.action.SNOOZE"
    const val ACTION_DISMISS = "com.mynominee.notify.action.DISMISS"
    const val ACTION_MARK_DONE = "com.mynominee.notify.action.MARK_DONE"
    const val ACTION_SHOW_SNOOZED = "com.mynominee.notify.action.SHOW_SNOOZED"

    private const val TAG = "NotificationHelper"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Reminder alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Time-sensitive reminder and alarm alerts"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 250, 150, 350)
            enableLights(true)
            lightColor = android.graphics.Color.rgb(200, 62, 255)
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun showReminderNotification(
        context: Context,
        reminderId: Long,
        title: String,
        description: String?,
        snoozeMinutes: Int = 10
    ) {
        createNotificationChannel(context)

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Notification permission is not granted; reminder $reminderId was not posted.")
            return
        }
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            Log.w(TAG, "Notifications are disabled; reminder $reminderId was not posted.")
            return
        }

        val alertIntent = Intent(context, FullScreenReminderActivity::class.java).apply {
            action = ACTION_OPEN_ALERT
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_DESCRIPTION, description)
            putExtra(EXTRA_SNOOZE_MINUTES, snoozeMinutes.coerceAtLeast(1))
        }
        val alertPendingIntent = PendingIntent.getActivity(
            context,
            requestCode(reminderId, ACTION_OPEN_ALERT),
            alertIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notificationId = notificationIdFor(reminderId)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_reminder)
            .setLargeIcon(createLargeIcon(context))
            .setContentTitle("Reminder")
            .setContentText(title)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(description?.takeIf(String::isNotBlank) ?: "Time to start your day ✨")
                    .setBigContentTitle(title)
                    .setSummaryText("Notify")
            )
            .setColor(android.graphics.Color.rgb(200, 62, 255))
            .setColorized(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(true)
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(false)
            .setContentIntent(alertPendingIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Snooze ($snoozeMinutes min)",
                actionPendingIntent(context, ACTION_SNOOZE, reminderId, title, description, snoozeMinutes)
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Dismiss",
                actionPendingIntent(context, ACTION_DISMISS, reminderId, title, description, snoozeMinutes)
            )
            .addAction(
                android.R.drawable.checkbox_on_background,
                "Mark done",
                actionPendingIntent(context, ACTION_MARK_DONE, reminderId, title, description, snoozeMinutes)
            )

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT < 34 || manager.canUseFullScreenIntent()) {
            builder.setFullScreenIntent(alertPendingIntent, true)
        } else {
            Log.w(TAG, "Full-screen intent access is disabled; reminder will use a heads-up notification.")
        }

        try {
            manager.notify(notificationId, builder.build())
        } catch (exception: SecurityException) {
            Log.e(TAG, "Unable to post reminder notification $reminderId.", exception)
        }
    }

    fun actionPendingIntent(
        context: Context,
        action: String,
        reminderId: Long,
        title: String,
        description: String?,
        snoozeMinutes: Int
    ): PendingIntent {
        val intent = Intent(context, ReminderNotificationActionReceiver::class.java).apply {
            this.action = action
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_DESCRIPTION, description)
            putExtra(EXTRA_SNOOZE_MINUTES, snoozeMinutes.coerceAtLeast(1))
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode(reminderId, action),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun scheduleSnooze(
        context: Context,
        reminderId: Long,
        title: String,
        description: String?,
        snoozeMinutes: Int
    ): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = snoozedPendingIntent(
            context,
            reminderId,
            title,
            description,
            snoozeMinutes
        )
        val triggerAtMillis = System.currentTimeMillis() +
            snoozeMinutes.coerceAtLeast(1) * 60_000L

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            true
        } catch (exception: SecurityException) {
            Log.w(TAG, "Exact snooze alarm permission is unavailable; using an inexact alarm.", exception)
            try {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                true
            } catch (fallbackException: RuntimeException) {
                Log.e(TAG, "Unable to schedule snooze for reminder $reminderId.", fallbackException)
                false
            }
        } catch (exception: RuntimeException) {
            Log.e(TAG, "Unable to schedule snooze for reminder $reminderId.", exception)
            false
        }
    }

    fun cancelSnooze(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(snoozedPendingIntent(context, reminderId, null, null, 10))
    }

    fun cancelNotification(context: Context, reminderId: Long) {
        NotificationManagerCompat.from(context).cancel(notificationIdFor(reminderId))
    }

    fun notificationIdFor(reminderId: Long): Int =
        NOTIFICATION_ID_BASE + (reminderId % 1_000_000L).toInt()

    private fun snoozedPendingIntent(
        context: Context,
        reminderId: Long,
        title: String?,
        description: String?,
        snoozeMinutes: Int
    ): PendingIntent {
        val intent = Intent(context, ReminderNotificationActionReceiver::class.java).apply {
            action = ACTION_SHOW_SNOOZED
            putExtra(EXTRA_REMINDER_ID, reminderId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_DESCRIPTION, description)
            putExtra(EXTRA_SNOOZE_MINUTES, snoozeMinutes.coerceAtLeast(1))
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode(reminderId, ACTION_SHOW_SNOOZED),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun requestCode(reminderId: Long, action: String): Int {
        val foldedId = (reminderId xor (reminderId ushr 32)).toInt()
        return 31 * foldedId + action.hashCode()
    }

    private fun createLargeIcon(context: Context): Bitmap? {
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_notification_badge) ?: return null
        val width = drawable.intrinsicWidth.coerceAtLeast(1)
        val height = drawable.intrinsicHeight.coerceAtLeast(1)
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bitmap ->
            drawable.setBounds(0, 0, width, height)
            drawable.draw(Canvas(bitmap))
        }
    }
}
