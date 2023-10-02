package com.arnyminerz.filamagenta.core.remote

import androidx.annotation.WorkerThread
import com.arnyminerz.filamagenta.core.remote.result.auth.TokenResult
import com.arnyminerz.filamagenta.core.security.AccessToken
import com.arnyminerz.filamagenta.core.utils.uri.buildUpon
import org.json.JSONObject
import java.net.URI
import javax.net.ssl.HttpsURLConnection

class RemoteAuthentication private constructor(
    val host: String,
    private val clientId: String,
    private val clientSecret: String
) {
    companion object {
        @Volatile
        var instance: RemoteAuthentication? = null
            private set

        @Synchronized
        fun initialize(host: String, clientId: String, clientSecret: String) {
            instance = RemoteAuthentication(host, clientId, clientSecret)
        }

        fun getInstance(host: String, clientId: String, clientSecret: String): RemoteAuthentication =
            instance ?: synchronized(this) {
                instance ?: RemoteAuthentication(host, clientId, clientSecret).also { instance = it }
            }
    }

    @WorkerThread
    private fun <R> post(body: Map<String, String>, block: (HttpsURLConnection) -> R): R {
        val uri = URI.create("https://$host")
            .buildUpon()
            .appendPath("oauth")
            .appendPath("token")
            .build()

        val bodyData = body
            .map { (k, v) -> "$k=$v" }
            .joinToString("&")
        return URI.create(uri.toString())
            .openConnection(
                method = "POST",
                beforeConnection = {
                    it.setRequestProperty(
                        "Content-Type",
                        "application/x-www-form-urlencoded"
                    )
                }
            ) { connection ->
                connection.outputStream.use { it.write(bodyData.toByteArray()) }
                block(connection)
            }
    }

    @WorkerThread
    fun requestToken(code: String): TokenResult =
        post(
            body = mapOf(
                "grant_type" to "authorization_code",
                "code" to code,
                "client_id" to clientId,
                "client_secret" to clientSecret,
                "redirect_uri" to "app://filamagenta"
            )
        ) { connection ->
            val responseCode = connection.responseCode
            val responseMessage = connection.responseMessage

            val response = connection.inputStream.use { it.readBytes() }
            when (responseCode) {
                200 -> {
                    val json = JSONObject(response.decodeToString())
                    val token = AccessToken.fromJSON(json)

                    TokenResult.Success(token)
                }

                else -> TokenResult.Failure(responseCode, responseMessage)
            }
        }

    @WorkerThread
    fun refreshToken(refreshToken: String) =
        post(
            body = mapOf(
                "grant_type" to "refresh_token",
                "refresh_token" to refreshToken
            )
        ) { connection ->
            val responseCode = connection.responseCode
            val responseMessage = connection.responseMessage

            val response = connection.inputStream.use { it.readBytes() }
            when (responseCode) {
                200 -> {
                    val json = JSONObject(response.decodeToString())
                    val token = AccessToken.fromJSON(json)

                    TokenResult.Success(token)
                }

                else -> TokenResult.Failure(responseCode, responseMessage)
            }
        }
}
