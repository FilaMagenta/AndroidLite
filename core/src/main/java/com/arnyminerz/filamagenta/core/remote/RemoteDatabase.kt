package com.arnyminerz.filamagenta.core.remote

import com.arnyminerz.filamagenta.core.Logger
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

        Logger.d("Performing connection with $ip:$port...")
        connection = DriverManager.getConnection(url, username, password)
    }

    val isClosed: Boolean
        get() = connection.isClosed

    fun close() = connection.close()

    fun <T : Any> query(sql: String, predicate: (row: ResultSet) -> T): List<T> {
        Logger.d("SQL > $sql")
        val statement = connection.createStatement()
        val result = statement.executeQuery(sql)
        val list = arrayListOf<T>()
        while (result.next())
            list.add(predicate(result))
        return list
    }

    fun <T : Any> query(sql: String, parser: com.arnyminerz.filamagenta.core.database.prototype.RemoteDataParser<T>): List<T> =
        query(sql) { parser.parse(it) }

    fun <T : Any> query(
        table: String,
        select: Set<String>? = null,
        where: Map<String, Any>? = null,
        predicate: (row: ResultSet) -> T
    ): List<T> {
        val predicateQuery = select?.joinToString(", ") ?: "*"
        val whereQuery = where?.let { whereMap ->
            " WHERE " + whereMap
                .toList()
                .joinToString(", ") { (key, value) ->
                val quotedValue =
                    if (value is Int || value is Long || value is Float || value is Double)
                        value
                    else
                        "'$value'"
                "$key=$quotedValue"
            }
        } ?: ""
        return query("SELECT $predicateQuery FROM $table$whereQuery", predicate)
    }

    fun <T : Any> query(
        table: String,
        select: Set<String>? = null,
        where: Map<String, Any>? = null,
        parser: com.arnyminerz.filamagenta.core.database.prototype.RemoteDataParser<T>
    ): List<T> = query(table, select, where) { parser.parse(it) }

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