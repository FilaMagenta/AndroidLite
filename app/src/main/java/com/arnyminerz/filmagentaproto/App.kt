package com.arnyminerz.filmagentaproto

import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import com.arnyminerz.filmagentaproto.utils.PermissionsUtils.hasNotificationPermission
import kotlin.random.Random

class App : Application(), Observer<List<WorkInfo>> {
    override fun onCreate() {
        super.onCreate()

        SyncWorker.schedule(this)

        SyncWorker.getLiveState(this).observeForever(this)
    }

    @SuppressLint("MissingPermission")
    override fun onChanged(value: List<WorkInfo>) {
        if (!hasNotificationPermission(this))
            return

        for (info in value) {
            if (info.state.isFinished && info.state == WorkInfo.State.FAILED) {
                // If the job has failed, show a notification
                val notification = NotificationCompat.Builder(this, NotificationChannels.SYNC_ERROR)
                    .setSmallIcon(R.drawable.logo_magenta_mono)
                    .setContentTitle(getString(R.string.sync_error_title))
                    .setContentText(getString(R.string.sync_error_message))
                    .addAction(
                        R.drawable.round_share_24,
                        getString(R.string.share),
                        PendingIntent.getBroadcast(
                            this,
                            0,
                            Intent.createChooser(
                                Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "State: ${info.state}.\nOutput: ${info.outputData.keyValueMap}")
                                    type = "text/plain"
                                },
                                null,
                            ),
                            PendingIntent.FLAG_IMMUTABLE,
                        )
                    )
                    .build()
                NotificationManagerCompat.from(this).notify(Random.nextInt(), notification)
            }
        }
    }

    override fun onTerminate() {
        super.onTerminate()

        SyncWorker.getLiveState(this).removeObserver(this)
    }
}