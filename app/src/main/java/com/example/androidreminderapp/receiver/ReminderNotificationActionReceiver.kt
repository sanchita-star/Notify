package com.example.androidreminderapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.androidreminderapp.service.AlarmScheduler
import com.example.androidreminderapp.service.NotificationHelper
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ReminderNotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(NotificationHelper.EXTRA_REMINDER_ID, -1L)
        if (reminderId <= 0L) {
            Log.e(TAG, "Notification action arrived without a valid reminder ID.")
            return
        }

        val title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: "Reminder"
        val description = intent.getStringExtra(NotificationHelper.EXTRA_DESCRIPTION)
        val snoozeMinutes = intent.getIntExtra(NotificationHelper.EXTRA_SNOOZE_MINUTES, 10)
        val alarmSound = intent.getStringExtra(NotificationHelper.EXTRA_ALARM_SOUND) ?: "default"

        if (intent.action == NotificationHelper.ACTION_SHOW_SNOOZED) {
            NotificationHelper.showReminderNotification(
                context,
                reminderId,
                title,
                description,
                snoozeMinutes,
                alarmSound
            )
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            var dismissNotification = true
            try {
                when (intent.action) {
                    NotificationHelper.ACTION_SNOOZE -> {
                        dismissNotification = NotificationHelper.scheduleSnooze(
                            context,
                            reminderId,
                            title,
                            description,
                            snoozeMinutes,
                            alarmSound
                        )
                    }

                    NotificationHelper.ACTION_DISMISS -> {
                        NotificationHelper.cancelSnooze(context, reminderId)
                    }

                    NotificationHelper.ACTION_MARK_DONE -> {
                        markReminderDone(context, reminderId)
                    }

                    else -> {
                        dismissNotification = false
                        Log.w(TAG, "Ignoring unknown notification action: ${intent.action}")
                    }
                }
            } catch (exception: Exception) {
                dismissNotification = false
                Log.e(TAG, "Unable to process notification action for reminder $reminderId.", exception)
            } finally {
                if (dismissNotification) {
                    NotificationHelper.cancelNotification(context, reminderId)
                }
                pendingResult.finish()
            }
        }
    }

    private suspend fun markReminderDone(context: Context, reminderId: Long) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ReminderDaoEntryPoint::class.java
        )
        val userId = entryPoint.firebaseAuth().currentUser?.uid
        if (userId == null) {
            Log.w(TAG, "Cannot mark reminder $reminderId done because no user is signed in.")
            return
        }

        val dao = entryPoint.reminderDao()
        val entity = dao.getReminderById(reminderId)
        if (entity == null || entity.userId != userId) {
            Log.w(TAG, "Reminder $reminderId was not found for the signed-in user.")
            return
        }

        val completed = entity.copy(isActive = false, isCompleted = true)
        dao.updateReminder(completed)
        AlarmScheduler.cancelAlarm(context, reminderId)

        try {
            entryPoint.reminderCloudStore().save(userId, completed)
        } catch (exception: Exception) {
            Log.e(TAG, "Reminder $reminderId was completed locally but cloud sync failed.", exception)
        }
    }

    companion object {
        private const val TAG = "ReminderAction"
    }
}
