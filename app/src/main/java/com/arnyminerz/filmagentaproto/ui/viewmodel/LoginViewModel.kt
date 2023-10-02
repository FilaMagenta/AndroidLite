package com.arnyminerz.filmagentaproto.ui.viewmodel

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.app.Application
import android.net.Uri
import android.os.Bundle
import androidx.annotation.WorkerThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.arnyminerz.filamagenta.core.data.oauth.UserInformation
import com.arnyminerz.filamagenta.core.remote.openConnection
import com.arnyminerz.filamagenta.core.security.AccessToken
import com.arnyminerz.filamagenta.core.utils.ui
import com.arnyminerz.filmagentaproto.account.AccountHelper
import com.arnyminerz.filmagentaproto.account.Authenticator
import com.arnyminerz.filmagentaproto.account.credentials.Credentials
import com.arnyminerz.filmagentaproto.database.remote.RemoteDatabaseInterface
import com.arnyminerz.filmagentaproto.utils.async
import com.arnyminerz.filmagentaproto.utils.toURI
import com.arnyminerz.filmagentaproto.utils.toast
import com.arnyminerz.filmagentaproto.worker.SyncWorker
import org.json.JSONObject
import timber.log.Timber
import java.net.URI

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val am = AccountManager.get(application)

    val isRequestingToken = MutableLiveData(false)

    fun requestToken(activity: Activity, code: String) = async {
        val uri = Uri.parse("https://filamagenta.com")
            .buildUpon()
            .appendPath("oauth")
            .appendPath("token")
            .build()
        val body = mapOf(
            "grant_type" to "authorization_code",
            "code" to code,
            "client_id" to "QcQIPJWtmv8ViPdlJ8enJnMjH31HAOt5grgShShj",
            "client_secret" to "UXPzFM956IhA0IUtrdZa3PHrFSVrsZDp4LQtDMs8",
            "redirect_uri" to "app://filamagenta"
        )
            .map { (k, v) -> "$k=$v" }
            .joinToString("&")
        URI.create(uri.toString())
            .openConnection(
                method = "POST",
                beforeConnection = {
                    it.setRequestProperty(
                        "Content-Type",
                        "application/x-www-form-urlencoded"
                    )
                }
            ) { connection ->
                connection.outputStream.use { it.write(body.toByteArray()) }

                val responseCode = connection.responseCode
                val responseMessage = connection.responseMessage

                val response = connection.inputStream.use { it.readBytes() }
                when(responseCode) {
                    200 -> {
                        val json = JSONObject(response.decodeToString())
                        val token = AccessToken.fromJSON(json)

                        addAccount(activity, token)
                    }
                    else -> {
                        // TODO: warn the user
                        Timber.e("Login failed ($responseCode): $responseMessage")
                        ui { activity.toast("Login failed") }
                    }
                }
            }
    }

    @WorkerThread
    private suspend fun addAccount(activity: Activity, token: AccessToken) {
        val information = getUserInformation(token)

        AccountHelper.addAccount(am, information, token, activity)

        ui {
            activity.setResult(Activity.RESULT_OK)
            activity.finish()
        }
    }

    /**
     * Returns all the accounts the application has registered.
     */
    fun getAccounts(): Array<out Account> = am.getAccountsByType(Authenticator.AuthTokenType)

    private fun getUserInformation(token: AccessToken): UserInformation {
        if (token.isExpired()) throw IllegalStateException("The given token has expired.")

        return Uri.parse("https://filamagenta.com")
            .buildUpon()
            .appendPath("oauth")
            .appendPath("me")
            .appendQueryParameter("access_token", token.token)
            .build()
            .toURI()
            .openConnection("GET") { connection ->
                 connection.inputStream
                    .use { it.readBytes() }
                    .decodeToString()
                    .let { JSONObject(it) }
                    .let(UserInformation::fromJSON)
            }
    }
}
