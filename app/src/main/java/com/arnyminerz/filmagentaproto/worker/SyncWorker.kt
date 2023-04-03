package com.arnyminerz.filmagentaproto.worker

import android.accounts.Account
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.database.sqlite.SQLiteConstraintException
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.map
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest.Companion.MIN_BACKOFF_MILLIS
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.arnyminerz.filamagenta.core.data.Transaction
import com.arnyminerz.filamagenta.core.database.data.woo.ROLE_ADMINISTRATOR
import com.arnyminerz.filamagenta.core.database.data.woo.WooClass
import com.arnyminerz.filamagenta.core.utils.now
import com.arnyminerz.filamagenta.core.utils.trimmedAndCaps
import com.arnyminerz.filmagentaproto.NotificationChannels
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.account.Authenticator
import com.arnyminerz.filmagentaproto.account.Authenticator.Companion.USER_DATA_CUSTOMER_ADMIN
import com.arnyminerz.filmagentaproto.account.Authenticator.Companion.USER_DATA_CUSTOMER_ID
import com.arnyminerz.filmagentaproto.activity.ShareMessageActivity
import com.arnyminerz.filmagentaproto.database.local.AppDatabase
import com.arnyminerz.filmagentaproto.database.local.RemoteDatabaseDao
import com.arnyminerz.filmagentaproto.database.local.TransactionsDao
import com.arnyminerz.filmagentaproto.database.local.WooCommerceDao
import com.arnyminerz.filmagentaproto.database.remote.RemoteCommerce
import com.arnyminerz.filmagentaproto.database.remote.RemoteDatabaseInterface
import com.arnyminerz.filmagentaproto.utils.PermissionsUtils
import io.sentry.ITransaction
import io.sentry.Sentry
import io.sentry.SpanStatus
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import timber.log.Timber

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
        private const val WORKER_TAG = "sync_worker"
        const val TAG_PERIODIC = "periodic"

        const val MANUAL_SYNC_WORK_NAME = "manual_sync"

        private const val PERIODIC_WORK_NAME = "sync"

        private const val SYNC_CUSTOMERS = "sync_customers"

        private const val SYNC_ORDERS = "sync_orders"

        private const val SYNC_EVENTS = "sync_events"

        private const val SYNC_PAYMENTS = "sync_payments"

        private const val SYNC_TRANSACTIONS = "sync_transactions"

        private const val SYNC_SOCIOS = "sync_socios"

        private const val IGNORE_CACHE = "ignore_cache"

        const val PROGRESS_STEP = "step"

        const val PROGRESS = "progress"

        const val EXCEPTION_CLASS = "exception_class"
        const val EXCEPTION_MESSAGE = "exception_message"

        private const val NOTIFICATION_ID = 20230315
        private const val ERROR_NOTIFICATION_ID = 20230324

        /**
         * The number of attempts to do before giving up on synchronization.
         */
        private const val ATTEMPTS = 5

        fun schedule(context: Context) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .addTag(WORKER_TAG)
                .addTag(TAG_PERIODIC)
                .setConstraints(Constraints(NetworkType.CONNECTED))
                .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.MINUTES)
                .setInitialDelay(4, TimeUnit.HOURS)
                // .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    PERIODIC_WORK_NAME,
                    ExistingWorkPolicy.KEEP,
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
            ignoreCache: Boolean = false,
        ): UUID {
            val request: OneTimeWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .addTag(WORKER_TAG)
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
                        IGNORE_CACHE to ignoreCache,
                    )
                )
                .build()
            Timber.i("Enqueuing new work request.")
            WorkManager.getInstance(context).enqueueUniqueWork(
                MANUAL_SYNC_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request,
            )
            return request.id
        }

        fun getScheduledLiveState(context: Context) = WorkManager
            .getInstance(context)
            .getWorkInfosForUniqueWorkLiveData(PERIODIC_WORK_NAME)
            .map { it.firstOrNull() }

        fun getLiveStates(context: Context) = WorkManager
            .getInstance(context)
            .getWorkInfosByTagLiveData(WORKER_TAG)

        fun getLiveState(context: Context, uuid: UUID) = WorkManager
            .getInstance(context)
            .getWorkInfoByIdLiveData(uuid)
    }

    private val am = AccountManager.get(appContext)

    private lateinit var notificationManager: NotificationManagerCompat

    private lateinit var transactionsDao: TransactionsDao
    private lateinit var remoteDatabaseDao: RemoteDatabaseDao
    private lateinit var wooCommerceDao: WooCommerceDao

    private lateinit var transaction: ITransaction

    private var isFirstSynchronization = false

    private var ignoreCache = false

    override suspend fun doWork(): Result {
        Timber.i("Running Synchronization...")

        transaction = Sentry.startTransaction("SyncWorker", "synchronization")

        ignoreCache = inputData.getBoolean(IGNORE_CACHE, false)
        transaction.setData(IGNORE_CACHE, ignoreCache)

        notificationManager = NotificationManagerCompat.from(applicationContext)

        return try {
            synchronize()

            // Schedule a new work
            if (tags.contains(TAG_PERIODIC))
                schedule(applicationContext)

            return Result.success()
        } catch (e: Exception) {
            // Log the exception
            Timber.e(e, "Could not complete synchronization.")

            // Append the error to the transaction
            transaction.throwable = e
            transaction.status = SpanStatus.INTERNAL_ERROR

            // Notify Sentry about the error
            Sentry.captureException(e)

            // If reached ATTEMPTS, give up, return failure and show notification
            if (runAttemptCount >= ATTEMPTS) {
                // Show the error notification
                showErrorNotification(e)

                Result.failure(
                    workDataOf(
                        EXCEPTION_CLASS to e::class.java.name,
                        EXCEPTION_MESSAGE to e.message,
                    )
                )
            } else
                Result.retry()
        } finally {
            notificationManager.cancel(NOTIFICATION_ID)

            transaction.finish()
        }
    }

    /**
     * Runs the synchronization process for the app.
     */
    private suspend fun synchronize() {
        setProgress(ProgressStep.INITIALIZING)

        // Get access to the database
        val database = AppDatabase.getInstance(applicationContext)
        transactionsDao = database.transactionsDao()
        remoteDatabaseDao = database.remoteDatabaseDao()
        wooCommerceDao = database.wooCommerceDao()

        // Store if this is the first synchronization
        isFirstSynchronization = transactionsDao.getAll().isEmpty()

        val syncTransactions = inputData.getBoolean(SYNC_TRANSACTIONS, true)
        val syncSocios = inputData.getBoolean(SYNC_SOCIOS, true)

        // Synchronize data of all the accounts
        val accounts = am.getAccountsByType(Authenticator.AuthTokenType)
        for (account in accounts) {
            val authToken: String? = am.peekAuthToken(account, Authenticator.AuthTokenType)
            val dni = account.name

            if (authToken == null) {
                Timber.e("Credentials for $dni are not valid, clearing password...")
                am.clearPassword(account)
                continue
            }

            // Fetch the data from woo
            fetchAndUpdateWooData(account)

            // After fetchAndUpdateWooData, USER_DATA_CUSTOMER_ADMIN must be defined
            val isAdmin = am.getUserData(account, USER_DATA_CUSTOMER_ADMIN).toBoolean()

            // Synchronize transactions
            if (syncTransactions) {
                // Fetch the data and update the database
                setProgress(ProgressStep.SYNC_TRANSACTIONS)

                if (isAdmin) {
                    // If user is admin, synchronize transactions for all users
                    Timber.d("User is admin. Synchronizing all the transactions in database...")
                    synchronizeSocio(null)
                }

                val idSocio = RemoteDatabaseInterface.fetchIdSocioFromDni(dni)
                if (idSocio == null) {
                    Timber.w("Could not get idSocio for DNI=$dni")
                    continue
                }

                synchronizeSocio(idSocio)

                setProgress(ProgressStep.INTERMEDIATE)
            }
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
                    synchronizeSocio(associated.idSocio)
                } catch (e: Exception) {
                    Timber.e(
                        e, "Could not synchronize data for associated: ${associated.idSocio}",
                    )
                    continue
                }
            }
            setProgress(ProgressStep.INTERMEDIATE)
        }

        Timber.i("Finished synchronization")
    }

    /**
     * Synchronizes all the data for the socio with the given id.
     */
    private suspend fun synchronizeSocio(idSocio: Long?) {
        // If idSocio is not null, fetch only transactions with that id
        val transactions = idSocio?.let {
            RemoteDatabaseInterface.fetchTransactions(it)
        }
        // If idSocio is null, fetch all transactions
            ?: RemoteDatabaseInterface.fetchAllTransactions()
        // Name is used for notifications, notifications will only be sent for the user holding the
        // device's account. If idSocio is null, no notifications will be shown.
        val name = idSocio?.let { id ->
            RemoteDatabaseInterface.fetchSocio(id) {
                it.getString("Nombre") + " " + it.getString("Apellidos")
            }.first()
        }

        // Get all the transactions currently stored
        val oldTransactions = idSocio?.let { transactionsDao.getByIdSocio(it) }
            ?: transactionsDao.getAll()
        // Iterate all the new transactions
        for (transaction in transactions) {
            val oldTransaction = oldTransactions.find { transaction.id == it.id }
            if (oldTransaction != null) {
                // If the transaction already exists, update it
                transaction.notified = oldTransaction.notified
                transactionsDao.update(transaction)
            } else {
                // Otherwise, simply insert the new one
                transactionsDao.insert(transaction)
            }
        }

        // Show notifications for new transactions
        idSocio?.let { transactionsDao.getByIdSocio(it) }?.let {allTransactions ->
            for (transaction in allTransactions)
                if (!transaction.notified) {
                    // No notifications should be shown during the first synchronization
                    if (!isFirstSynchronization)
                        notifyTransaction(name!!, transaction)
                    transactionsDao.update(
                        transaction.copy(notified = true)
                    )
                }
        }
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
    private suspend inline fun <T : WooClass> fetchAndUpdateDatabase(
        shouldSyncInputKey: String,
        progressStep: ProgressStep,
        remoteFetcher: (cache: List<T>) -> List<T>,
        databaseFetcher: () -> List<T>,
        insertMethod: (item: T) -> Unit,
        updateMethod: (item: T) -> Unit,
        deleteMethod: (item: T) -> Unit,
        listExtraProcessing: (List<T>) -> Unit = {},
    ) {
        val span = transaction.startChild("fetchAndUpdateDatabase", progressStep.name)
        val shouldSync = inputData.getBoolean(shouldSyncInputKey, true)
        if (shouldSync) {
            setProgress(progressStep)
            Timber.d("Getting list from remote...")
            val list = remoteFetcher(databaseFetcher())

            listExtraProcessing(list)

            Timber.d("Updating database...")
            for ((index, item) in list.withIndex()) {
                setProgress(progressStep, index to list.size)
                try {
                    insertMethod(item)
                } catch (e: SQLiteConstraintException) {
                    updateMethod(item)
                }
            }

            Timber.d("Synchronizing deletions...")
            val storedList = databaseFetcher()
            for (stored in storedList)
                if (list.find { it.id == stored.id } == null)
                    deleteMethod(stored)

            setProgress(ProgressStep.INTERMEDIATE)
        }
        span.finish()
    }

    /**
     * Fetches all the data from the REST endpoints, and updates the database accordingly.
     */
    private suspend fun fetchAndUpdateWooData(
        account: Account,
    ) {
        val span = transaction.startChild("fetchAndUpdateWooData")
        val dni = account.name

        var customerId: Long? = am.getUserData(account, USER_DATA_CUSTOMER_ID)?.toLongOrNull()
        var isAdmin: Boolean? = am.getUserData(account, USER_DATA_CUSTOMER_ADMIN)?.toBoolean()

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
            val customer = customers.find { it.username.equals(dni, true) }
                ?: throw IndexOutOfBoundsException("Could not find logged in user in the customers database.")
            if (customerId == null) {
                Timber.i("Customer ID: ${customer.id}")
                customerId = customer.id
                am.setUserData(account, USER_DATA_CUSTOMER_ID, customerId.toString())
            }
            if (isAdmin == null) {
                Timber.i("Customer role: ${customer.role}")
                isAdmin = customer.role == ROLE_ADMINISTRATOR
                am.setUserData(account, USER_DATA_CUSTOMER_ADMIN, isAdmin.toString())
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
                { RemoteCommerce.orderList(customerId?.takeIf { isAdmin != true }) },
                { wooCommerceDao.getAllOrders() },
                { wooCommerceDao.insert(it) },
                { wooCommerceDao.update(it) },
                { wooCommerceDao.delete(it) },
            )

        // Fetch all events available
        fetchAndUpdateDatabase(
            SYNC_EVENTS,
            ProgressStep.SYNC_EVENTS,
            { cachedEvents ->
                RemoteCommerce.eventList(
                    // If ignoreCache is true, pass empty list
                    cachedEvents.takeIf { !ignoreCache } ?: emptyList(),
                ) { progress ->
                    setProgress(
                        ProgressStep.SYNC_EVENTS,
                        progress
                    )
                }
            },
            { wooCommerceDao.getAllEvents() },
            { wooCommerceDao.insert(it) },
            { wooCommerceDao.update(it) },
            { wooCommerceDao.delete(it) },
        )

        span.finish()
    }

    /**
     * Creates the required notification channels.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannels() {
        NotificationChannels.createSyncGroup(applicationContext)
        NotificationChannels.createSyncProgressChannel(applicationContext)
        NotificationChannels.createSyncErrorChannel(applicationContext)
        NotificationChannels.createTransactionChannel(applicationContext)
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
        try {
            setForeground(
                createForegroundInfo(step, progress)
            )
        } catch (e: IllegalStateException) {
            Timber.i("Cannot update foreground info since progress is in background.")
        }
    }

    /**
     * Shows a notification when an error occurs during synchronization.
     */
    @SuppressLint("MissingPermission")
    private fun showErrorNotification(exception: java.lang.Exception) {
        if (!PermissionsUtils.hasNotificationPermission(applicationContext)) return

        val message = listOf(
            "Exception: ${exception::class.java.name}",
            "Message: ${exception.message}"
        )

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, ShareMessageActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra(ShareMessageActivity.EXTRA_MESSAGE, message.joinToString("\n"))
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification =
            NotificationCompat.Builder(applicationContext, NotificationChannels.SYNC_ERROR)
                .setSmallIcon(R.drawable.logo_magenta_mono)
                .setContentTitle(applicationContext.getString(R.string.sync_error_title))
                .setContentText(applicationContext.getString(R.string.sync_error_message))
                .addAction(
                    R.drawable.round_share_24,
                    applicationContext.getString(R.string.share),
                    pendingIntent,
                )
                .build()
        notificationManager.notify(ERROR_NOTIFICATION_ID, notification)
    }

    /**
     * Sends a notification to the user regarding a transaction received or charged to the user's
     * account.
     */
    @SuppressLint("MissingPermission")
    private fun notifyTransaction(accountName: String, transaction: Transaction) {
        if (!PermissionsUtils.hasNotificationPermission(applicationContext)) return

        val message = if (transaction.income)
            applicationContext.getString(
                R.string.notification_transaction_input_message,
                transaction.price,
                transaction.concept,
            )
        else
            applicationContext.getString(
                R.string.notification_transaction_charge_message,
                transaction.price,
                transaction.concept,
            )

        val notification =
            NotificationCompat.Builder(applicationContext, NotificationChannels.TRANSACTION)
                .setSmallIcon(R.drawable.logo_magenta_mono)
                .setContentTitle(applicationContext.getString(R.string.notification_transaction_title))
                .setContentText(message)
                .setContentInfo(accountName)
                .setWhen(transaction.date.time)
                .setShowWhen((now().time - transaction.date.time) > 24 * 60 * 1000)
                .build()
        notificationManager.notify(Random.nextInt(), notification)
    }
}