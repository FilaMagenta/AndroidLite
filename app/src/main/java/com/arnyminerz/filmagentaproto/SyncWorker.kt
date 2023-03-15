package com.arnyminerz.filmagentaproto

import android.accounts.Account
import android.accounts.AccountManager
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
import com.arnyminerz.filmagentaproto.database.data.woo.WooClass
import com.arnyminerz.filmagentaproto.database.local.AppDatabase
import com.arnyminerz.filmagentaproto.database.local.PersonalDataDao
import com.arnyminerz.filmagentaproto.database.local.RemoteDatabaseDao
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

    private val am = AccountManager.get(appContext)

    private lateinit var personalDataDao: PersonalDataDao
    private lateinit var remoteDatabaseDao: RemoteDatabaseDao
    private lateinit var wooCommerceDao: WooCommerceDao

    override suspend fun doWork(): Result {
        Log.i(TAG, "Running Synchronization...")
        setProgress(ProgressStep.INITIALIZING)

        // Get access to the database
        val database = AppDatabase.getInstance(applicationContext)
        personalDataDao = database.personalDataDao()
        remoteDatabaseDao = database.remoteDatabaseDao()
        wooCommerceDao = database.wooCommerceDao()

        val syncTransactions = inputData.getBoolean(SYNC_TRANSACTIONS, true)
        val syncSocios = inputData.getBoolean(SYNC_SOCIOS, true)

        // Synchronize data of all the accounts
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
            fetchAndUpdateWooData(account)
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

    /**
     * Takes the extra data from [shouldSyncInputKey], and if it's `true` (`true` by default), it
     * fetches the data from the server (using the provided function [remoteFetcher]), and updates
     * the database using [insertMethod] and [updateMethod]. Then, obtains all the stored data from
     * the database using [databaseFetcher], and deletes the entries deleted from the server with
     * [updateMethod]. Also sends progress updates with [setProgress] and [progressStep].
     * @param shouldSyncInputKey The key from [getInputData] which should be a boolean value stating
     * whether this field should be fetched. `true` by default.
     * @param progressStep One of [ProgressStep] for sending progress updates with [setProgress].
     * @param remoteFetcher Should return all the entries from the server.
     * @param databaseFetcher Should return all the entries from the local database.
     * @param insertMethod Should insert the given `item` into the database.
     * @param updateMethod Should update the given `item` in the database.
     * @param deleteMethod Should delete the given `item` from the database.
     * @param listExtraProcessing If some extra processing wants to be done with the entries fetched
     * with [remoteFetcher].
     */
    private suspend inline fun <T: WooClass> fetchAndUpdateDatabase(
        shouldSyncInputKey: String,
        progressStep: ProgressStep,
        remoteFetcher: () -> List<T>,
        databaseFetcher: () -> List<T>,
        insertMethod: (item: T) -> Unit,
        updateMethod: (item: T) -> Unit,
        deleteMethod: (item: T) -> Unit,
        listExtraProcessing: (List<T>) -> Unit = {},
    ) {
        val shouldSync = inputData.getBoolean(shouldSyncInputKey, true)
        if (shouldSync) {
            setProgress(progressStep)
            Log.d(TAG, "Getting list from remote...")
            val list = remoteFetcher()

            listExtraProcessing(list)

            Log.d(TAG, "Updating database...")
            for ((index, item) in list.withIndex()) {
                setProgress(progressStep, index to list.size)
                try {
                    insertMethod(item)
                } catch (e: SQLiteConstraintException) {
                    updateMethod(item)
                }
            }

            Log.d(TAG, "Synchronizing deletions...")
            val storedList = databaseFetcher()
            for (stored in storedList)
                if (list.find { it.id == stored.id } == null)
                    deleteMethod(stored)

            setProgress(ProgressStep.INTERMEDIATE)
        }
    }

    /**
     * Fetches all the data from the REST endpoints, and updates the database accordingly.
     */
    private suspend fun fetchAndUpdateWooData(
        account: Account,
    ) {
        val dni = am.getPassword(account)

        var customerId: Long? = am.getUserData(account, "customer_id")?.toLongOrNull()

        // Fetch all customers data
        fetchAndUpdateDatabase(
            SYNC_CUSTOMERS,
            ProgressStep.SYNC_CUSTOMERS,
            { RemoteCommerce.customersList() },
            { wooCommerceDao.getAllCustomers() },
            { wooCommerceDao.insert(it) },
            { wooCommerceDao.update(it) },
            { wooCommerceDao.delete(it) },
        ) { customers ->
            if (customerId == null) {
                val customer = customers.find { it.username.equals(dni, true) }
                    ?: throw IndexOutOfBoundsException("Could not find logged in user in the customers database.")
                Log.i(TAG, "Customer ID: ${customer.id}")
                customerId = customer.id
                am.setUserData(account, "customer_id", customerId.toString())
            }
        }

        // Fetch all payments available
        fetchAndUpdateDatabase(
            SYNC_PAYMENTS,
            ProgressStep.SYNC_PAYMENTS,
            { RemoteCommerce.paymentsList() },
            { wooCommerceDao.getAllAvailablePayments() },
            { wooCommerceDao.insert(it) },
            { wooCommerceDao.update(it) },
            { wooCommerceDao.delete(it) },
        )

        // Fetch all orders available
        if (customerId != null)
            fetchAndUpdateDatabase(
                SYNC_ORDERS,
                ProgressStep.SYNC_ORDERS,
                { RemoteCommerce.orderList(customerId!!) },
                { wooCommerceDao.getAllOrders() },
                { wooCommerceDao.insert(it) },
                { wooCommerceDao.update(it) },
                { wooCommerceDao.delete(it) },
            )

        // Fetch all events available
        fetchAndUpdateDatabase(
            SYNC_EVENTS,
            ProgressStep.SYNC_EVENTS,
            { RemoteCommerce.eventList { progress -> setProgress(ProgressStep.SYNC_EVENTS, progress) } },
            { wooCommerceDao.getAllEvents() },
            { wooCommerceDao.insert(it) },
            { wooCommerceDao.update(it) },
            { wooCommerceDao.delete(it) },
        )
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