package com.arnyminerz.filmagentaproto.database.remote

import androidx.annotation.WorkerThread
import com.arnyminerz.filmagentaproto.BuildConfig
import com.arnyminerz.filmagentaproto.database.remote.protos.Socio

object RemoteDatabaseInterface {
    @WorkerThread
    fun fetchAll(): List<Socio> {
        var database: RemoteDatabase? = null
        try {
            database = RemoteDatabase(
                BuildConfig.DB_HOSTNAME,
                BuildConfig.DB_PORT,
                BuildConfig.DB_DATABASE,
                BuildConfig.DB_USERNAME,
                BuildConfig.DB_PASSWORD,
            )
            return database.query("SELECT * FROM tbSocios ts;", Socio)
        } finally {
            database?.close()
        }
    }
}