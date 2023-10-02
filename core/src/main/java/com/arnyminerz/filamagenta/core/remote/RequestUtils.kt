package com.arnyminerz.filamagenta.core.remote

import com.arnyminerz.filamagenta.core.Logger
import java.net.URI
import javax.net.ssl.HttpsURLConnection

inline fun <R> URI.openConnection(
    method: String,
    beforeConnection: (HttpsURLConnection) -> Unit = {},
    block: (HttpsURLConnection) -> R,
): R {
    Logger.d("$method > $this")
    val url = toURL()
    val connection = url.openConnection() as HttpsURLConnection
    connection.requestMethod = method
    connection.readTimeout = 45 * 1000
    connection.connectTimeout = 20 * 1000
    connection.instanceFollowRedirects = true

    beforeConnection(connection)

    return try {
        connection.connect()

        block(connection)
    } catch (e: Exception) {
        throw e
    } finally {
        connection.disconnect()
    }
}
