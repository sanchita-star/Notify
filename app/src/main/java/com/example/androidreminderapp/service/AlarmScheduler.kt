package com.example.androidreminderapp.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.androidreminderapp.domain.model.Reminder
import com.example.androidreminderapp.receiver.AlarmReceiver

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"

    const val EXTRA_REMINDER_ID = "extra_reminder_id"
    const val EXTRA_EXPECTED_TRIGGER = "extra_expected_trigger"
    const val EXTRA_REMINDER_TITLE = "extra_reminder_title"
    const val EXTRA_REMINDER_DESC = "extra_reminder_desc"

    private fun getPendingIntentRequestCode(reminderId: Long): Int {
        return (reminderId xor (reminderId ushr 32)).toInt()
    }

    fun scheduleAlarm(context: Context, reminder: Reminder, triggerAtMillis: Long): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_EXPECTED_TRIGGER, triggerAtMillis)
            putExtra(EXTRA_REMINDER_TITLE, reminder.title)
            putExtra(EXTRA_REMINDER_DESC, reminder.description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getPendingIntentRequestCode(reminder.id),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val hasExactAlarmAccess =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (!hasExactAlarmAccess) {
            Log.w(TAG, "Exact alarm access is not granted; scheduling reminder ${reminder.id} inexactly.")
            return scheduleInexact(alarmManager, triggerAtMillis, pendingIntent, reminder.id)
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            return true
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm was rejected for reminder ${reminder.id}; retrying inexactly.", e)
            return scheduleInexact(alarmManager, triggerAtMillis, pendingIntent, reminder.id)
        } catch (e: RuntimeException) {
            Log.e(TAG, "Unable to schedule exact alarm for reminder ${reminder.id}.", e)
            return false
        }
    }

    fun cancelAlarm(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            getPendingIntentRequestCode(reminderId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        NotificationHelper.cancelSnooze(context, reminderId)
        NotificationHelper.cancelNotification(context, reminderId)
    }

    private fun scheduleInexact(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent,
        reminderId: Long
    ): Boolean {
        return try {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            true
        } catch (exception: RuntimeException) {
            Log.e(TAG, "Unable to schedule inexact alarm for reminder $reminderId.", exception)
            false
        }
    }
}
