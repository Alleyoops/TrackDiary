package com.alleyoop.diary


import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.hasExtra(NotificationHandler.INTENT_EXTRA_NOTIFICATION)) handleNotification(context)
        if (Intent.ACTION_BOOT_COMPLETED == intent.action ||
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED == intent.action
        ) { setNotification(context) }
    }

    private fun setNotification(context: Context) {
        val notificationHandler = NotificationHandler()
        notificationHandler.scheduleNotification(context)
    }

    private fun handleNotification(context: Context) {
        val notificationHandler = NotificationHandler()
        notificationHandler.showNotification(context)
    }
}