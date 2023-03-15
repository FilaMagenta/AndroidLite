package com.arnyminerz.filmagentaproto

import android.accounts.Account
import android.accounts.AccountManager
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.database.sqlite.SQLiteConstraintException
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest.Companion.MIN_BACKOFF_MILLIS
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.arnyminerz.filmagentaproto.account.Authenticator
import com.arnyminerz.filmagentaproto.database.data.PersonalData
import com.arnyminerz.filmagentaproto.database.local.AppDatabase
import com.arnyminerz.filmagentaproto.database.local.WooCommerceDao
import com.arnyminerz.filmagentaproto.database.remote.RemoteCommerce
import com.arnyminerz.filmagentaproto.database.remote.RemoteDatabaseInterface
import com.arnyminerz.filmagentaproto.database.remote.RemoteServer
import com.arnyminerz.filmagentaproto.utils.trimmedAndCaps
import java.util.concurrent.TimeUnit

enum class ProgressStep(@StringRes val textRes: Int) {
    INITIALIZING(R.string.sync_step_initializing),
    SYNC_CUSTOMERS(R.string.sync_step_customers),
    SYNC_ORDERS(R.string.sync_step_orders),
    SYNC_EVENTS(R.string.sync_step_events),
    SYNC_PAYMENTS(R.string.sync_step_payments),
    SYNC_TRANSACTIONS(R.string.sync_step_transactions),
    SYNC_SOCIOS(R.string.sync_step_socios),
    INTERMEDIATE(R.string.sync_step_intermediate)
}

class SyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "sync_worker"

        private const val UNIQUE_WORK_NAME = "sync"

        private const val SYNC_CUSTOMERS = "sync_customers"

        private const val SYNC_ORDERS = "sync_orders"

        private const val SYNC_EVENTS = "sync_events"

        private const val SYNC_PAYMENTS = "sync_payments"

        private const val SYNC_TRANSACTIONS = "sync_transactions"

        private const val SYNC_SOCIOS = "sync_socios"

        const val PROGRESS_STEP = "step"

        const val PROGRESS = "progress"

        private const val NOTIFICATION_ID = 20230315

        fun schedule(context: Context) {
            val request = PeriodicWorkRequest
                .Builder(
                    SyncWorker::class.java,
                    8,
                    TimeUnit.HOURS,
                    15,
                    TimeUnit.MINUTES,
                )
                .addTag(TAG)
                .setConstraints(Constraints(NetworkType.CONNECTED))
                .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    UNIQUE_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request,
                )
        }

        fun run(
            context: Context,
            syncTransactions: Boolean = true,
            syncSocios: Boolean = true,
            syncCustomers: Boolean = true,
            syncOrders: Boolean = true,
            syncEvents: Boolean = true,
            syncPayments: Boolean = true,
        ): Operation {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .addTag(TAG)
                .setConstraints(Constraints(NetworkType.CONNECTED))
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .setInputData(
                    workDataOf(
                        SYNC_TRANSACTIONS to syncTransactions,
                        SYNC_SOCIOS to syncSocios,
                        SYNC_CUSTOMERS to syncCustomers,
                        SYNC_ORDERS to syncOrders,
                        SYNC_EVENTS to syncEvents,
                        SYNC_PAYMENTS to syncPayments,
                    )
                )
                .build()
            return WorkManager.getInstance(context)
                .enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }

        fun getLiveState(context: Context) = WorkManager
            .getInstance(context)
            .getWorkInfosForUniqueWorkLiveData(UNIQUE_WORK_NAME)
    }

    private val notificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result {
        Log.i(TAG, "Running Synchronization...")
        setProgress(ProgressStep.INITIALIZING)

        // Get access to the database
        val database = AppDatabase.getInstance(applicationContext)
        val personalDataDao = database.personalDataDao()
        val remoteDatabaseDao = database.remoteDatabaseDao()
        val wooCommerceDao = database.wooCommerceDao()

        val syncTransactions = inputData.getBoolean(SYNC_TRANSACTIONS, true)
        val syncSocios = inputData.getBoolean(SYNC_SOCIOS, true)

        // Synchronize data of all the accounts
        val am = AccountManager.get(applicationContext)
        val accounts = am.getAccountsByType(Authenticator.AuthTokenType)
        accounts.forEach { account ->
            val authToken: String? = am.peekAuthToken(account, Authenticator.AuthTokenType)

            if (authToken == null) {
                Log.e(TAG, "Credentials for ${account.name} are not valid, clearing password...")
                am.clearPassword(account)
                return@forEach
            }

            if (syncTransactions) {
                // Fetch the data and update the database
                setProgress(ProgressStep.SYNC_TRANSACTIONS)

                val html = RemoteServer.fetch(authToken)
                val data = PersonalData.fromHtml(html, account)
                val dbData = personalDataDao.getByAccount(account.name, account.type)
                if (dbData == null)
                    personalDataDao.insert(data)
                else
                    personalDataDao.update(dbData)

                setProgress(ProgressStep.INTERMEDIATE)
            }

            // Fetch the data from woo
            fetchAndUpdateWooData(am, account, wooCommerceDao)
        }

        // Fetch all the data from users in database
        if (syncSocios) {
            setProgress(ProgressStep.SYNC_SOCIOS)
            val socios = RemoteDatabaseInterface.fetchAll()
            for ((index, socio) in socios.withIndex()) {
                setProgress(ProgressStep.SYNC_SOCIOS, index to socios.size)
                try {
                    remoteDatabaseDao.insert(socio)
                } catch (e: SQLiteConstraintException) {
                    remoteDatabaseDao.update(socio)
                }
            }
            setProgress(ProgressStep.INTERMEDIATE)
        }

        // Also fetch the data of all the associated accounts
        if (syncTransactions) {
            setProgress(ProgressStep.SYNC_TRANSACTIONS)
            for ((index, account) in accounts.withIndex()) {
                setProgress(ProgressStep.SYNC_TRANSACTIONS, index to accounts.size)

                val dni = am.getPassword(account).trimmedAndCaps
                val socios = remoteDatabaseDao.getAll()
                val socio = socios.find { it.Dni?.trimmedAndCaps == dni } ?: continue
                val associateds = remoteDatabaseDao.getAllAssociatedWith(socio.idSocio)
                if (associateds.isEmpty()) continue

                // Iterate each associated, and log in with their credentials to fetch the data
                for (associated in associateds) try {
                    // Log in with the user's credentials
                    val associatedDni = associated.Dni ?: continue
                    Log.d(TAG, "Logging in with \"${associated.Nombre}\" and $associatedDni")
                    val authToken = RemoteServer.login(associated.Nombre, associatedDni)
                    // Fetch the data for the associated
                    val html = RemoteServer.fetch(authToken)
                    val data = PersonalData.fromHtml(
                        html,
                        Account(associated.Nombre, Authenticator.AuthTokenType)
                    )
                    try {
                        personalDataDao.insert(data)
                    } catch (e: SQLiteConstraintException) {
                        personalDataDao.update(data)
                    }
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "Could not synchronize data for associated: ${associated.idSocio}",
                        e,
                    )
                    continue
                }
            }
            setProgress(ProgressStep.INTERMEDIATE)
        }

        return Result.success()
    }

    private suspend fun fetchAndUpdateWooData(
        am: AccountManager,
        account: Account,
        wooCommerceDao: WooCommerceDao,
    ) {
        val dni = am.getPassword(account)

        var customerId: Long? = am.getUserData(account, "customer_id")?.toLongOrNull()

        // Fetch all customers data
        val shouldSyncCustomers = inputData.getBoolean(SYNC_CUSTOMERS, true)
        if (shouldSyncCustomers) {
            setProgress(ProgressStep.SYNC_CUSTOMERS)
            Log.d(TAG, "Getting customers data...")
            val customers = RemoteCommerce.customersList()
            Log.d(TAG, "Got ${customers.size} customers.")

            if (customerId == null) {
                val customer = customers.find { it.username.equals(dni, true) }
                    ?: throw IndexOutOfBoundsException("Could not find logged in user in the customers database.")
                Log.i(TAG, "Customer ID: ${customer.id}")
                customerId = customer.id
                am.setUserData(account, "customer_id", customerId.toString())
            }

            Log.d(TAG, "Updating customers in database...")
            for ((index, item) in customers.withIndex()) {
                setProgress(ProgressStep.SYNC_CUSTOMERS, index to customers.size)
                try {
                    wooCommerceDao.insert(item)
                } catch (e: SQLiteConstraintException) {
                    wooCommerceDao.update(item)
                }
            }
            setProgress(ProgressStep.INTERMEDIATE)
        }

        // Fetch all payments available
        val shouldSyncPayments = inputData.getBoolean(SYNC_PAYMENTS, true)
        if (shouldSyncPayments) {
            setProgress(ProgressStep.SYNC_PAYMENTS)
            Log.d(TAG, "Getting available payments list...")
            val payments = RemoteCommerce.paymentsList()
            Log.d(TAG, "Updating available payments in database...")
            for ((index, item) in payments.withIndex()) {
                setProgress(ProgressStep.SYNC_PAYMENTS, index to payments.size)
                try {
                    wooCommerceDao.insert(item)
                } catch (e: SQLiteConstraintException) {
                    wooCommerceDao.update(item)
                }
            }
            setProgress(ProgressStep.INTERMEDIATE)
        }

        // Fetch all orders available
        val shouldSyncOrders = inputData.getBoolean(SYNC_ORDERS, true)
        if (shouldSyncOrders && customerId != null) {
            setProgress(ProgressStep.SYNC_ORDERS)
            Log.d(TAG, "Getting orders list...")
            val orders = RemoteCommerce.orderList(customerId)
            Log.d(TAG, "Updating orders in database...")
            for ((index, item) in orders.withIndex()) {
                setProgress(ProgressStep.SYNC_ORDERS, index to orders.size)
                try {
                    wooCommerceDao.insert(item)
                } catch (e: SQLiteConstraintException) {
                    wooCommerceDao.update(item)
                }
            }
        }

        // Fetch all events available
        val shouldSyncEvents = inputData.getBoolean(SYNC_EVENTS, true)
        if (shouldSyncEvents) {
            setProgress(ProgressStep.SYNC_EVENTS)
            Log.d(TAG, "Getting events list...")
            val events = RemoteCommerce.eventList()
            Log.d(TAG, "Updating events in database...")
            for ((index, item) in events.withIndex()) {
                setProgress(ProgressStep.SYNC_EVENTS, index to events.size)
                try {
                    wooCommerceDao.insert(item)
                } catch (e: SQLiteConstraintException) {
                    wooCommerceDao.update(item)
                }
            }
            setProgress(ProgressStep.INTERMEDIATE)
        }
    }

    /**
     * Creates the required notification channels.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannels() {
        NotificationChannels.createSyncGroup(applicationContext)
        NotificationChannels.createSyncProgressChannel(applicationContext)
        NotificationChannels.createSyncErrorChannel(applicationContext)
    }

    private fun createForegroundInfo(
        step: ProgressStep,
        progress: Pair<Int, Int>?
    ): ForegroundInfo {
        val cancel = applicationContext.getString(R.string.cancel)
        val cancelIntent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(id)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannels()
        }

        val notification =
            NotificationCompat.Builder(applicationContext, NotificationChannels.SYNC_PROGRESS)
                .setContentTitle(applicationContext.getString(R.string.sync_running))
                .setContentText(applicationContext.getString(step.textRes))
                .apply {
                    progress?.let { (current, max) ->
                        setProgress(max, current, false)
                        setTicker("$current / $max")
                    } ?: {
                        setProgress(0, 0, true)
                        setTicker(null)
                    }
                }
                .setSmallIcon(R.drawable.logo_magenta_mono)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_delete, cancel, cancelIntent)
                .build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        else
            ForegroundInfo(NOTIFICATION_ID, notification)
    }

    /**
     * Updates the progress of the worker.
     * @param step The step currently being ran.
     * @param progress The current progress reported, if any. Can be null. First is current, second
     * is max.
     */
    private suspend fun setProgress(step: ProgressStep, progress: Pair<Int, Int>? = null) {
        setProgress(
            workDataOf(
                PROGRESS_STEP to step.name,
                PROGRESS to progress?.let { (current, max) -> current.toDouble() / max.toDouble() },
            )
        )
        setForeground(
            createForegroundInfo(step, progress)
        )
    }
}