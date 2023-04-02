package com.arnyminerz.filmagentaproto.utils

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.arnyminerz.filmagentaproto.database.local.AppDatabase
import java.io.IOException
import org.junit.After
import org.junit.Before

abstract class DatabaseTest {
    protected val context: Context by lazy {
        ApplicationProvider.getApplicationContext()
    }

    protected val db: AppDatabase by lazy {
        Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @Before
    fun prepare_database() {
        AppDatabase.setInstance(db)
    }

    @After
    @Throws(IOException::class)
    fun close_db() {
        db.close()
    }
}
