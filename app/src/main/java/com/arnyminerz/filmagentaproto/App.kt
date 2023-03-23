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
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroid
import io.sentry.protocol.User
import kotlin.random.Random
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.map

class App : Application(), OnAccountsUpdateListener, FlowCollector<Int> {
    companion object {
        private const val TAG = "App"
    }

    private lateinit var am: AccountManager

    private var accounts: Array<out Account>? = null

    private var selectedAccountIndex = 0

    override fun onCreate() {
        super.onCreate()

        // Initialize Sentry
        SentryAndroid.init(this) { options ->
            options.dsn = BuildConfig.SENTRY_DSN
        }

        // Schedule the SyncWorker to run automatically
        SyncWorker.schedule(this)

        // Start observing the currently selected account index
        doAsync {
            dataStore.data
                .map { it[SELECTED_ACCOUNT] ?: 0 }
                .collect(this@App)
        }

        am = AccountManager.get(this)
        am.addOnAccountsUpdatedListener(this, HandlerCompat.createAsync(mainLooper), true)
    }

    override suspend fun emit(value: Int) {
        Log.d(TAG, "Changed selected account to index $value")
        selectedAccountIndex = value

        updateUserId()
    }

    override fun onAccountsUpdated(accounts: Array<out Account>?) {
        Log.d(TAG, "Accounts list updated. There are ${accounts?.size} accounts.")
        this.accounts = accounts

        updateUserId()
    }

    /**
     * Updates the currently tracking user data from [accounts] and [selectedAccountIndex].
     */
    private fun updateUserId() {
        // Fetch the account's data
        val account = accounts?.getOrNull(selectedAccountIndex) ?: return
        val customerId = am.getUserData(account, "customer_id")
        val dni = am.getPassword(account)

        // Update the tracking user
        val user = User().apply {
            id = customerId
            username = account.name
        }
        Sentry.setUser(user)

        Log.i(TAG, "Updated user reference to: ${account.name} ($dni, $customerId)")
    }
}