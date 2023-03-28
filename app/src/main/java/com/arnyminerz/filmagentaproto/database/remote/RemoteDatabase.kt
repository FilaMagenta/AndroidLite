package com.arnyminerz.filmagentaproto.database.remote

import android.os.StrictMode
import com.arnyminerz.filmagentaproto.database.prototype.RemoteDataParser
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import timber.log.Timber

class RemoteDatabase(
    ip: String,
    port: Int,
    database: String,
    username: String,
    password: String,
) {
    companion object {
        private const val Classes = "net.sourceforge.jtds.jdbc.Driver"
    }

    private val url = "jdbc:jtds:sqlserver://$ip:$port/$database"

    private val connection: Connection

    init {
        Class.forName(Classes)

        val policy = StrictMode.ThreadPolicy.Builder()
            .permitAll()
            .build()
        StrictMode.setThreadPolicy(policy)

        Timber.d("Performing connection with $ip:$port...")
        connection = DriverManager.getConnection(url, username, password)
    }

    val isClosed: Boolean
        get() = connection.isClosed

    fun close() = connection.close()

    fun <T : Any> query(sql: String, predicate: (row: ResultSet) -> T): List<T> {
        Timber.d("SQL > $sql")
        val statement = connection.createStatement()
        val result = statement.executeQuery(sql)
        val list = arrayListOf<T>()
        while (result.next())
            list.add(predicate(result))
        return list
    }

    fun <T : Any> query(sql: String, parser: RemoteDataParser<T>): List<T> = query(sql) { parser.parse(it) }

    /**
     * Runs the SQL update query stated in [sql].
     * @return The amount of rows updated.
     */
    fun update(sql: String): Int {
        val statement = connection.createStatement()
        return statement.executeUpdate(sql)
    }

    fun <R> use(block: RemoteDatabase.() -> R): R = block(this).also { close() }
}