package com.arnyminerz.filmagentaproto.worker

import android.accounts.AccountManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.arnyminerz.filmagentaproto.account.AccountHelper
import com.arnyminerz.filmagentaproto.database.remote.RemoteDatabaseInterface
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/*TODO class TestSyncWorker {
    private val context: Context by lazy {
        ApplicationProvider.getApplicationContext()
    }

    private val am: AccountManager by lazy {
        AccountManager.get(context)
    }

    private val nm: NotificationManagerCompat by lazy {
        NotificationManagerCompat.from(context)
    }

    /** Inserts some sample accounts to use for the tests */
    @Before
    fun prepareAccount() {
        AccountHelper.addAccount(am, "12345678X", "password", "token", null)
    }

    @Before
    fun prepareMockk() {
        mockkObject(RemoteDatabaseInterface)
        every { RemoteDatabaseInterface.fetchIdSocioFromDni(any()) } answers { Random.nextLong() }

        mockk<SyncWorker> {
            // Mock all database functions, we insert the data manually
            coEvery { fetchAndUpdateWooData(any()) } answers {
                println("called fetchAndUpdateWooData(...)")
            }
        }
    }

    @Test
    fun testSyncWorker_transactions() {
        val worker = TestListenableWorkerBuilder<SyncWorker>(context)
            .setInputData(
                workDataOf(
                    SyncWorker.SYNC_TRANSACTIONS to true,
                    SyncWorker.SYNC_SOCIOS to false,
                    SyncWorker.SYNC_CUSTOMERS to false,
                    SyncWorker.SYNC_ORDERS to false,
                    SyncWorker.SYNC_EVENTS to false,
                    SyncWorker.SYNC_PAYMENTS to false,
                    // Ignore cache, force pull
                    SyncWorker.IGNORE_CACHE to true,
                )
            )
            .build()
        runBlocking {
            // Run the worker
            val result = worker.doWork()

            // Make sure no errors happened
            assertEquals(result, ListenableWorker.Result.success())
        }
    }

    @After
    fun dismissNotifications() {
        nm.cancel(SyncWorker.NOTIFICATION_ID)
        nm.cancel(SyncWorker.ERROR_NOTIFICATION_ID)
    }
}*/