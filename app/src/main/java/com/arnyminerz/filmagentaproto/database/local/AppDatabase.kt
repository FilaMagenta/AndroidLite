package com.arnyminerz.filmagentaproto.database.local

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.arnyminerz.filamagenta.core.data.Transaction
import com.arnyminerz.filamagenta.core.data.tb.Socio
import com.arnyminerz.filamagenta.core.database.data.woo.AvailablePayment
import com.arnyminerz.filamagenta.core.database.data.woo.Customer
import com.arnyminerz.filamagenta.core.database.data.woo.Event
import com.arnyminerz.filamagenta.core.database.data.woo.Order
import com.arnyminerz.filmagentaproto.database.data.admin.CodeScanned
import com.arnyminerz.filmagentaproto.database.data.woo.WooConverters
import com.arnyminerz.filmagentaproto.database.local.migration.Migration5To6
import com.arnyminerz.filmagentaproto.database.local.migration.Migration6To7
import com.arnyminerz.filmagentaproto.database.local.migration.Migration8To9
import com.arnyminerz.filmagentaproto.worker.SyncWorker
import timber.log.Timber

@Database(
    entities = [
        Transaction::class, Socio::class, Event::class, Order::class, Customer::class,
        AvailablePayment::class, CodeScanned::class
    ],
    version = 9,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6, spec = Migration5To6::class),
        // Updates ScannedCode storage and data.
        AutoMigration(from = 6, to = 7, spec = Migration6To7::class),
        // Added payment information to order
        AutoMigration(from = 7, to = 8),
        // Migrated dates to Java 8
        AutoMigration(from = 8, to = 9),
    ]
)
@TypeConverters(Converters::class, WooConverters::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        @VisibleForTesting(otherwise = VisibleForTesting.NONE)
        fun setInstance(database: AppDatabase) = synchronized(AppDatabase) {
            INSTANCE = database
        }

        fun getInstance(context: Context) = INSTANCE ?: synchronized(AppDatabase) {
            INSTANCE ?: Room
                .databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "magenta-proto",
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Timber.i("Running synchronization after db creation.")
                        SyncWorker.run(context)
                    }

                    override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                        super.onDestructiveMigration(db)
                        Timber.i("Running synchronization after db destructive migration.")
                        SyncWorker.run(context)
                    }
                })
                .addMigrations(Migration8To9)
                .build()
                .also { INSTANCE = it }
        }
    }

    abstract fun transactionsDao(): TransactionsDao

    abstract fun remoteDatabaseDao(): RemoteDatabaseDao

    abstract fun wooCommerceDao(): WooCommerceDao

    abstract fun adminDao(): AdminDao
}