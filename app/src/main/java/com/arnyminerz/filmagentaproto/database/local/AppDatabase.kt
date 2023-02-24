package com.arnyminerz.filmagentaproto.database.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.arnyminerz.filmagentaproto.database.data.PersonalData
import com.arnyminerz.filmagentaproto.database.remote.protos.Socio

@Database(
    entities = [PersonalData::class, Socio::class],
    version = 1,
)
@TypeConverters(Converters::class)
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
                .build()
                .also { INSTANCE = it }
        }
    }

    abstract fun personalDataDao(): PersonalDataDao

    abstract fun remoteDatabaseDao(): RemoteDatabaseDao
}