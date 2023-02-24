package com.arnyminerz.filmagentaproto.database.remote

import android.net.Uri
import android.util.Log
import com.arnyminerz.filmagentaproto.BuildConfig
import com.arnyminerz.filmagentaproto.exceptions.WrongCredentialsException
import java.io.IOException
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "RemoteServer"

object RemoteServer {
    private fun Uri.toURL(): URL = URL(toString())

    private val loginEndpoint = Uri.Builder()
        .scheme("https")
        .authority(BuildConfig.HOST)
        .appendPath("appMagenta")
        .appendPath("log_movil.php")
        .build()
        .toURL()

    private val mainEndpoint = Uri.Builder()
        .scheme("https")
        .authority(BuildConfig.HOST)
        .appendPath("appMagenta")
        .appendPath("fulla_movil.php")
        .build()
        .toURL()

    /**
     * Tries to log in with the given credentials.
     * @param name The user's name.
     * @param nif The user's NIF (password).
     * @return The token returned by the server.
     * @throws IOException If any error occurs while making the request.
     * @throws WrongCredentialsException If the credentials given are not correct.
     */
    suspend fun login(name: String, nif: String): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "Trying to log in at $loginEndpoint...")
        val connection = loginEndpoint.openConnection() as HttpsURLConnection
        connection.requestMethod = "POST"
        connection.readTimeout = 60 * 1000
        connection.connectTimeout = 60 * 1000
        connection.instanceFollowRedirects = false

        val body = "usuario=$name&passw=$nif".toByteArray()
        connection.setRequestProperty("Content-Length", body.size.toString())
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

        connection.outputStream.write(body)
        try {
            Log.d(TAG, "Connecting to server...")
            connection.connect()

            when(connection.responseCode) {
                in 300 until 400 -> {
                    // Only redirections are correct, check where the redirection leads
                    val location = connection.getHeaderField("Location")
                    Log.v(TAG, "Got redirection to: $location")
                    // If leading to condiciones_movil.php, the credentials were correct
                    if (location == "condiciones_movil.php") {
                        // Extract all set cookies
                        val setCookie = connection.getHeaderField("set-cookie")
                        // Parse the contents
                        val cookies = setCookie.split(";")
                            .filter { it.contains("=") }
                            .associate { pair -> pair.split("=").let { it[0] to it[1] } }
                        // Get the PHP session id cookie
                        cookies.getValue("PHPSESSID")
                    } else {
                        // Invalid credentials
                        throw WrongCredentialsException()
                    }
                }
                else -> throw IOException("Response error: ${connection.responseCode}")
            }
        } finally {
            Log.d(TAG, "Closing connection...")
            connection.disconnect()
        }
    }

    suspend fun fetch(token: String) = withContext(Dispatchers.IO) {
        Log.d(TAG, "Trying to log in at $mainEndpoint...")
        val connection = mainEndpoint.openConnection() as HttpsURLConnection
        connection.requestMethod = "POST"
        connection.readTimeout = 60 * 1000
        connection.connectTimeout = 60 * 1000
        connection.instanceFollowRedirects = true
        connection.setRequestProperty("Cookie", "PHPSESSID=$token")
        try {
            Log.d(TAG, "Connecting to server...")
            connection.connect()

            when(connection.responseCode) {
                in 200 until 300 -> {
                    // Get the response
                    connection.inputStream.use { it.bufferedReader().readText() }
                }
                else -> throw IOException("Response error: ${connection.responseCode}")
            }
        } finally {
            Log.d(TAG, "Closing connection...")
            connection.disconnect()
        }
    }
}
