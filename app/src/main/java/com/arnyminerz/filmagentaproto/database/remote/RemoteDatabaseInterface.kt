package com.arnyminerz.filmagentaproto.database.remote

import androidx.annotation.WorkerThread
import com.arnyminerz.filmagentaproto.BuildConfig
import com.arnyminerz.filmagentaproto.database.remote.protos.Socio
import java.sql.SQLException
import timber.log.Timber

object RemoteDatabaseInterface {
    private inline fun <R> interact(block: (RemoteDatabase) -> R): R {
        var database: RemoteDatabase? = null
        try {
            Timber.d("Initializing connection with database...")
            database = RemoteDatabase(
                BuildConfig.DB_HOSTNAME,
                BuildConfig.DB_PORT,
                BuildConfig.DB_DATABASE,
                BuildConfig.DB_USERNAME,
                BuildConfig.DB_PASSWORD,
            )
            Timber.d("Running database interaction...")
            return block(database)
        } catch (e: Exception) {
            throw e
        } finally {
            database?.close()
        }
    }

    @WorkerThread
    fun fetchAll(): List<Socio> = interact { database ->
        database.query("SELECT * FROM tbSocios ts;", Socio)
    }

    /**
     * Gets the hash and salt stored for the given DNI. First is hash, second is salt.
     * @return The hash and salt stored, or null if any.
     */
    @WorkerThread
    fun getHashForDni(dni: String) = interact { database ->
        Timber.d("Getting Hash and Salt from database...")
        database.query("SELECT Hash, Salt FROM mHashes WHERE Dni='$dni';") { resultSet ->
            Timber.d("Got a match in the server for $dni.")
            resultSet.getString("Hash") to resultSet.getString("Salt")
        }.firstOrNull().also {
            if (it == null) Timber.d("Got null response from database.")
        }
    }

    /**
     * Creates a new entry for the hash of the given DNI.
     * @throws SQLException If there's an error while running the request.
     */
    @WorkerThread
    fun addHashForDni(dni: String, hash: String, salt: String) = interact { database ->
        Timber.d("Setting hash for $dni...")
        database.update("INSERT INTO mHashes (Dni, Hash, Salt) VALUES ('$dni', '$hash', '$salt  ');")
    }
}