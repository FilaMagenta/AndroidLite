package com.arnyminerz.filmagentaproto

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.OnAccountsUpdateListener
import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.content.pm.ShortcutManagerCompat.FLAG_MATCH_DYNAMIC
import androidx.core.graphics.drawable.IconCompat
import androidx.core.os.HandlerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.arnyminerz.filmagentaproto.Shortcuts.ADMIN_PANEL_ID
import com.arnyminerz.filmagentaproto.Shortcuts.ADMIN_PANEL_LABEL_RES
import com.arnyminerz.filmagentaproto.account.Authenticator
import com.arnyminerz.filmagentaproto.activity.AdminActivity
import com.arnyminerz.filmagentaproto.database.data.woo.Customer
import com.arnyminerz.filmagentaproto.database.data.woo.ROLE_ADMINISTRATOR
import com.arnyminerz.filmagentaproto.database.local.AppDatabase
import com.arnyminerz.filmagentaproto.database.local.WooCommerceDao
import com.arnyminerz.filmagentaproto.storage.SELECTED_ACCOUNT
import com.arnyminerz.filmagentaproto.storage.dataStore
import com.arnyminerz.filmagentaproto.utils.doAsync
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroid
import io.sentry.protocol.User
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.map

class App : Application(), OnAccountsUpdateListener, FlowCollector<Int> {
    companion object {
        private const val TAG = "App"
    }

    private lateinit var am: AccountManager

    private val accountsLiveData = MutableLiveData<List<Account>>()

    val accounts: LiveData<List<Account>>
        get() = accountsLiveData

    private val customerLiveData = MutableLiveData<Customer>()

    val customer: LiveData<Customer>
        get() = customerLiveData

    private var selectedAccountIndex = 0

    private lateinit var wooCommerceDao: WooCommerceDao

    override fun onCreate() {
        super.onCreate()

        // Initialize Sentry
        SentryAndroid.init(this) { options ->
            options.dsn = BuildConfig.SENTRY_DSN
        }

        // Schedule the SyncWorker to run automatically
        SyncWorker.schedule(this)

        // Instantiate the Dao
        wooCommerceDao = AppDatabase.getInstance(this).wooCommerceDao()

        // Start observing the currently selected account index
        doAsync {
            dataStore.data
                .map { it[SELECTED_ACCOUNT] ?: 0 }
                .collect(this@App)
        }

        am = AccountManager.get(this)
        am.addOnAccountsUpdatedListener(this, HandlerCompat.createAsync(mainLooper), true)
    }

    override fun onTerminate() {
        super.onTerminate()

        am.removeOnAccountsUpdatedListener(this)
    }

    override suspend fun emit(value: Int) {
        Log.d(TAG, "Changed selected account to index $value")
        selectedAccountIndex = value

        updateUserId()
        updateAdminQuickAccessShortcut()
    }

    override fun onAccountsUpdated(accounts: Array<out Account>?) {
        Log.d(TAG, "Accounts list updated. There are ${accounts?.size} accounts.")
        val filteredAccounts = accounts?.filter { it.type == Authenticator.AuthTokenType }
        accountsLiveData.postValue(filteredAccounts)

        updateUserId()
        updateAdminQuickAccessShortcut()
    }

    /**
     * Updates the currently tracking user data from [accountsLiveData] and [selectedAccountIndex].
     */
    private fun updateUserId() {
        // Fetch the account's data
        val account = accountsLiveData.value?.getOrNull(selectedAccountIndex) ?: return
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

    /**
     * Adds or removes the admin panel shortcut according to the currently selected user.
     */
    private fun updateAdminQuickAccessShortcut() {
        doAsync {
            dataStore
                .data
                .map { preferences -> preferences[SELECTED_ACCOUNT] ?: 0 }
                .map { index ->
                    accounts.value?.getOrNull(index)?.let { account ->
                        val customerId: Long = am.getUserData(account, "customer_id")
                            ?.toLongOrNull() ?: return@let null
                        wooCommerceDao.getAllCustomers().find { it.id == customerId }
                    }
                }
                .collect { customer ->
                    // Update the customer state
                    customerLiveData.postValue(customer)

                    // If no customer, or not an admin, remove
                    if (customer == null || customer.role != ROLE_ADMINISTRATOR)
                        return@collect ShortcutManagerCompat.removeDynamicShortcuts(
                            this@App,
                            listOf(ADMIN_PANEL_ID),
                        )
                    val shortcuts = ShortcutManagerCompat.getShortcuts(this@App, FLAG_MATCH_DYNAMIC)

                    // Do not create multiple times
                    if (shortcuts.find { it.id == ADMIN_PANEL_ID } != null)
                        return@collect

                    // Create the shortcut for the admin panel
                    val shortcut = ShortcutInfoCompat.Builder(this@App, ADMIN_PANEL_ID)
                        .setShortLabel(getString(ADMIN_PANEL_LABEL_RES))
                        .setLongLabel(getString(ADMIN_PANEL_LABEL_RES))
                        .setIcon(IconCompat.createWithResource(this@App, R.mipmap.ic_launcher))
                        .setIntent(
                            Intent(this@App, AdminActivity::class.java).apply {
                                action = Intent.ACTION_VIEW
                            }
                        )
                        .build()
                    ShortcutManagerCompat.pushDynamicShortcut(this@App, shortcut)
                }
        }
    }
}