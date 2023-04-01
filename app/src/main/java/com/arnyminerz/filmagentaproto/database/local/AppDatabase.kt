package com.arnyminerz.filmagentaproto.database.local

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.arnyminerz.filmagentaproto.database.data.Transaction
import com.arnyminerz.filmagentaproto.database.data.admin.CodeScanned
import com.arnyminerz.filmagentaproto.database.data.woo.AvailablePayment
import com.arnyminerz.filmagentaproto.database.data.woo.Customer
import com.arnyminerz.filmagentaproto.database.data.woo.Event
import com.arnyminerz.filmagentaproto.database.data.woo.Order
import com.arnyminerz.filmagentaproto.database.data.woo.WooConverters
import com.arnyminerz.filmagentaproto.database.local.migration.Migration5To6
import com.arnyminerz.filmagentaproto.database.local.migration.Migration6To7
import com.arnyminerz.filmagentaproto.database.remote.protos.Socio
import com.arnyminerz.filmagentaproto.worker.SyncWorker

@Database(
    entities = [
        Transaction::class, Socio::class, Event::class, Order::class, Customer::class,
        AvailablePayment::class, CodeScanned::class
    ],
    version = 7,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6, spec = Migration5To6::class),
        // Updates ScannedCode storage and data.
        AutoMigration(from = 6, to = 7, spec = Migration6To7::class),
    ]
)
@TypeConverters(Converters::class, WooConverters::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

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
                        SyncWorker.run(context)
                    }

                    override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                        super.onDestructiveMigration(db)
                        SyncWorker.run(context)
                    }
                })
                .build()
                .also { INSTANCE = it }
        }
    }

    abstract fun transactionsDao(): TransactionsDao

    abstract fun remoteDatabaseDao(): RemoteDatabaseDao

    abstract fun wooCommerceDao(): WooCommerceDao

    abstract fun adminDao(): AdminDao
}