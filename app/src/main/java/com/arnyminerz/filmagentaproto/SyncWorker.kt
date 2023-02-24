package com.arnyminerz.filmagentaproto

import android.accounts.AccountManager
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.Operation
import androidx.work.WorkManager
import androidx.work.WorkRequest.Companion.MIN_BACKOFF_MILLIS
import androidx.work.WorkerParameters
import com.arnyminerz.filmagentaproto.account.Authenticator
import com.arnyminerz.filmagentaproto.database.data.PersonalData
import com.arnyminerz.filmagentaproto.database.local.AppDatabase
import com.arnyminerz.filmagentaproto.database.remote.RemoteDatabaseInterface
import com.arnyminerz.filmagentaproto.database.remote.RemoteServer
import java.util.concurrent.TimeUnit

class SyncWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {
    companion object {
        private const val TAG = "sync_worker"

        fun run(context: Context): Operation {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .addTag(TAG)
                .setConstraints(Constraints(NetworkType.CONNECTED))
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
            return WorkManager.getInstance(context)
                .enqueue(request)
        }
    }

    override suspend fun doWork(): Result {
        // Get access to the database
        val database = AppDatabase.getInstance(applicationContext)
        val personalDataDao = database.personalDataDao()
        val remoteDatabaseDao = database.remoteDatabaseDao()

        // Synchronize data of all the accounts
        val am = AccountManager.get(applicationContext)
        am.getAccountsByType(Authenticator.AuthTokenType).forEach { account ->
            val authToken = am.peekAuthToken(account, Authenticator.AuthTokenType)

            // Fetch the data and update the database
            val html = RemoteServer.fetch(authToken)
            val data = PersonalData.fromHtml(html, account)
            val dbData = personalDataDao.getByAccount(account.name, account.type)
            if (dbData == null)
                personalDataDao.insert(data)
            else
                personalDataDao.update(dbData)
        }

        // Fetch all the data from users in database
        RemoteDatabaseInterface.fetchAll().forEach { socio ->
            try {
                remoteDatabaseDao.insert(socio)
            } catch (e: SQLiteConstraintException) {
                remoteDatabaseDao.update(socio)
            }
        }

        return Result.success()
    }
}