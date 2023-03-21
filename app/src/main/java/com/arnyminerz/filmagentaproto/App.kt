package com.arnyminerz.filmagentaproto

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.OnAccountsUpdateListener
import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.HandlerCompat
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import com.arnyminerz.filmagentaproto.storage.SELECTED_ACCOUNT
import com.arnyminerz.filmagentaproto.storage.dataStore
import com.arnyminerz.filmagentaproto.utils.PermissionsUtils.hasNotificationPermission
import com.arnyminerz.filmagentaproto.utils.doAsync
import com.bugsnag.android.Bugsnag
import kotlin.random.Random
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.map

class App : Application(), Observer<List<WorkInfo>>, OnAccountsUpdateListener, FlowCollector<Int> {
    companion object {
        private const val TAG = "App"
    }

    private lateinit var am: AccountManager

    private var accounts: Array<out Account>? = null

    private var selectedAccountIndex = 0

    override fun onCreate() {
        super.onCreate()

        Bugsnag.start(this)

        SyncWorker.schedule(this)

        SyncWorker.getLiveState(this).observeForever(this)

        // Start observing the currently selected account index
        doAsync {
            dataStore.data
                .map { it[SELECTED_ACCOUNT] ?: 0 }
                .collect(this@App)
        }

        am = AccountManager.get(this)
        am.addOnAccountsUpdatedListener(this, HandlerCompat.createAsync(mainLooper), true)
    }

    /**
     * Sends notifications about the errors that might have occurred in the [SyncWorker].
     */
    @SuppressLint("MissingPermission")
    override fun onChanged(value: List<WorkInfo>) {
        Log.d(TAG, "Updated worker info. Count: ${value.size}")
        if (!hasNotificationPermission(this)) return

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

    override suspend fun emit(value: Int) {
        Log.d(TAG, "Changed selected account to index $value")
        selectedAccountIndex = value

        updateBugsnagUserId()
    }

    override fun onAccountsUpdated(accounts: Array<out Account>?) {
        Log.d(TAG, "Accounts list updated. There are ${accounts?.size} accounts.")
        this.accounts = accounts

        updateBugsnagUserId()
    }

    /**
     * Updates the currently tracking Bugsnag user data from [accounts] and [selectedAccountIndex].
     */
    private fun updateBugsnagUserId() {
        // Fetch the account's data
        val account = accounts?.getOrNull(selectedAccountIndex) ?: return
        val customerId = am.getUserData(account, "customer_id")
        val dni = am.getPassword(account)

        // Update the Bugsnag user
        Bugsnag.setUser(customerId, dni, account.name)
        Log.i(TAG, "Updated Bugsnag user reference to: ${account.name} ($dni, $customerId)")
    }

    override fun onTerminate() {
        super.onTerminate()

        SyncWorker.getLiveState(this).removeObserver(this)
    }
}