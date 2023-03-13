package com.arnyminerz.filmagentaproto

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
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

    override suspend fun doWork(): Result {
        Log.i(TAG, "Running Synchronization...")

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
                val html = RemoteServer.fetch(authToken)
                val data = PersonalData.fromHtml(html, account)
                val dbData = personalDataDao.getByAccount(account.name, account.type)
                if (dbData == null)
                    personalDataDao.insert(data)
                else
                    personalDataDao.update(dbData)
            }

            // Fetch the data from woo
            fetchAndUpdateWooData(am, account, wooCommerceDao)
        }

        // Fetch all the data from users in database
        if (syncSocios)
            RemoteDatabaseInterface.fetchAll().forEach { socio ->
                try {
                    remoteDatabaseDao.insert(socio)
                } catch (e: SQLiteConstraintException) {
                    remoteDatabaseDao.update(socio)
                }
            }

        // Also fetch the data of all the associated accounts
        if (syncTransactions)
            for (account in accounts) {
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
            for (item in customers)
                try {
                    wooCommerceDao.insert(item)
                } catch (e: SQLiteConstraintException) {
                    wooCommerceDao.update(item)
                }
        }

        // Fetch all payments available
        val shouldSyncPayments = inputData.getBoolean(SYNC_PAYMENTS, true)
        if (shouldSyncPayments) {
            Log.d(TAG, "Getting available payments list...")
            val payments = RemoteCommerce.paymentsList()
            Log.d(TAG, "Updating available payments in database...")
            for (item in payments)
                try {
                    wooCommerceDao.insert(item)
                } catch (e: SQLiteConstraintException) {
                    wooCommerceDao.update(item)
                }
        }

        // Fetch all orders available
        val shouldSyncOrders = inputData.getBoolean(SYNC_ORDERS, true)
        if (shouldSyncOrders && customerId != null) {
            Log.d(TAG, "Getting orders list...")
            val orders = RemoteCommerce.orderList(customerId)
            Log.d(TAG, "Updating orders in database...")
            for (item in orders)
                try {
                    wooCommerceDao.insert(item)
                } catch (e: SQLiteConstraintException) {
                    wooCommerceDao.update(item)
                }
        }

        // Fetch all events available
        val shouldSyncEvents = inputData.getBoolean(SYNC_EVENTS, true)
        if (shouldSyncEvents) {
            Log.d(TAG, "Getting events list...")
            val events = RemoteCommerce.eventList()
            Log.d(TAG, "Updating events in database...")
            for (item in events)
                try {
                    wooCommerceDao.insert(item)
                } catch (e: SQLiteConstraintException) {
                    wooCommerceDao.update(item)
                }
        }
    }
}