package com.arnyminerz.filmagentaproto.database.remote

import android.os.StrictMode
import android.util.Log
import com.arnyminerz.filmagentaproto.database.prototype.RemoteDataParser
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

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

        connection = DriverManager.getConnection(url, username, password)
    }

    val isClosed: Boolean
        get() = connection.isClosed

    fun close() = connection.close()

    fun <T : Any> query(sql: String, predicate: (row: ResultSet) -> T): List<T> {
        val statement = connection.createStatement()
        val result = statement.executeQuery(sql)
        val list = arrayListOf<T>()
        while (result.next())
            list.add(predicate(result))
        return list
    }

    fun <T : Any> query(sql: String, parser: RemoteDataParser<T>): List<T> = query(sql) { parser.parse(it) }
}