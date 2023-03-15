package com.arnyminerz.filmagentaproto

import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

object NotificationChannels {
    const val SYNC_GROUP_ID = "sync_group"

    const val SYNC_PROGRESS = "sync_progress"

    const val SYNC_ERROR = "sync_error"

    /**
     * Works as a shortcut for fetching the [NotificationManager] from a context.
     */
    private fun Context.notificationManager(): NotificationManager =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @RequiresApi(Build.VERSION_CODES.O)
    fun createSyncGroup(context: Context) = with(context) {
        val name = getString(R.string.notification_group_sync_name)
        val description = getString(R.string.notification_group_sync_desc)
        val group = NotificationChannelGroup(SYNC_GROUP_ID, name).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                this.description = description
        }
        notificationManager().createNotificationChannelGroup(group)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createSyncProgressChannel(context: Context) = with(context) {
        val name = getString(R.string.notification_channel_sync_name)
        val description = getString(R.string.notification_channel_sync_desc)
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(SYNC_PROGRESS, name, importance).apply {
            this.description = description
            this.group = SYNC_GROUP_ID
        }
        notificationManager().createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createSyncErrorChannel(context: Context) = with(context) {
        val name = getString(R.string.notification_channel_sync_error_name)
        val description = getString(R.string.notification_channel_sync_error_desc)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(SYNC_ERROR, name, importance).apply {
            this.description = description
            this.group = SYNC_GROUP_ID
        }
        notificationManager().createNotificationChannel(channel)
    }
}