package com.arnyminerz.filamagenta.core.remote

import com.arnyminerz.filamagenta.core.Logger
import com.arnyminerz.filamagenta.core.data.Transaction
import com.arnyminerz.filamagenta.core.data.tb.Socio
import java.sql.ResultSet
import java.sql.SQLException

abstract class RemoteDatabaseInterfaceProto {
    protected abstract val dbHostname: String
    protected abstract val dbPort: Int
    protected abstract val dbDatabase: String
    protected abstract val dbUsername: String
    protected abstract val dbPassword: String

    private inline fun <R> interact(block: (RemoteDatabase) -> R): R {
        var database: RemoteDatabase? = null
        try {
            Logger.d("Initializing connection with database...")
            database = RemoteDatabase(
                dbHostname,
                dbPort,
                dbDatabase,
                dbUsername,
                dbPassword,
            )
            Logger.d("Running database interaction...")
            return block(database)
        } catch (e: Exception) {
            throw e
        } finally {
            database?.close()
        }
    }

    fun fetchAll(): List<Socio> = interact { database ->
        database.query("SELECT * FROM tbSocios ts;", Socio)
    }

    /**
     * Fetches all the transactions stored in the database.
     */
    fun fetchAllTransactions() = interact { database ->
        database.query(
            "tbApuntesSocios",
            where = null,
            parser = Transaction.Companion
        )
    }

    /**
     * Fetches all the transactions made by the user with the given id.
     */
    fun fetchTransactions(idSocio: Long) = interact { database ->
        database.query(
            "tbApuntesSocios",
            where = mapOf("idSocio" to idSocio),
            parser = Transaction.Companion
        )
    }

    /**
     * Fetches the idSocio column for the given DNI.
     * @return The id of the socio with the given DNI, or null if not found or not a number.
     */
    fun fetchIdSocioFromDni(dni: String) = interact { database ->
        database.query(
            "tbSocios",
            select = setOf("idSocio"),
            where = mapOf("Dni" to dni),
            predicate = { it.getString("idSocio") }
        ).firstOrNull()?.toLongOrNull()
    }

    fun <T: Any> fetchSocio(idSocio: Long, predicate: (row: ResultSet) -> T) = interact { database ->
        database.query("tbSocios", select = null, where = mapOf("idSocio" to idSocio), predicate)
    }

    /**
     * Gets the hash and salt stored for the given DNI. First is hash, second is salt.
     * @return The hash and salt stored, or null if any.
     */
    fun getHashForDni(dni: String) = interact { database ->
        Logger.d("Getting Hash and Salt from database...")
        database.query("SELECT Hash, Salt FROM mHashes WHERE Dni='$dni';") { resultSet ->
            Logger.d("Got a match in the server for $dni.")
            resultSet.getString("Hash") to resultSet.getString("Salt")
        }.firstOrNull().also {
            if (it == null) Logger.d("Got null response from database.")
        }
    }

    /**
     * Creates a new entry for the hash of the given DNI.
     * @throws SQLException If there's an error while running the request.
     */
    fun addHashForDni(dni: String, hash: String, salt: String) = interact { database ->
        Logger.d("Setting hash for $dni...")
        database.update("INSERT INTO mHashes (Dni, Hash, Salt) VALUES ('$dni', '$hash', '$salt  ');")
    }
}