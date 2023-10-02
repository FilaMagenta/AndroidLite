package com.arnyminerz.filmagentaproto.ui.viewmodel

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.app.Application
import android.net.Uri
import androidx.annotation.WorkerThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.arnyminerz.filamagenta.core.data.oauth.UserInformation
import com.arnyminerz.filamagenta.core.remote.RemoteAuthentication
import com.arnyminerz.filamagenta.core.remote.openConnection
import com.arnyminerz.filamagenta.core.remote.result.auth.TokenResult
import com.arnyminerz.filamagenta.core.security.AccessToken
import com.arnyminerz.filamagenta.core.utils.ui
import com.arnyminerz.filmagentaproto.BuildConfig
import com.arnyminerz.filmagentaproto.account.AccountHelper
import com.arnyminerz.filmagentaproto.account.Authenticator
import com.arnyminerz.filmagentaproto.utils.async
import com.arnyminerz.filmagentaproto.utils.toURI
import com.arnyminerz.filmagentaproto.utils.toast
import org.json.JSONObject
import timber.log.Timber

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val am = AccountManager.get(application)

    val isRequestingToken = MutableLiveData(false)

    private val remoteAuthentication by lazy {
        RemoteAuthentication.getInstance(
            BuildConfig.HOST,
            BuildConfig.OAUTH_CLIENT_ID,
            BuildConfig.OAUTH_CLIENT_SECRET
        )
    }

    fun requestToken(activity: Activity, code: String) = async {
        val result = remoteAuthentication.requestToken(code)
        if (result is TokenResult.Success) {
            addAccount(activity, result.token)
        } else if (result is TokenResult.Failure) {
            val (responseCode, responseMessage) = result

            // TODO: warn the user
            Timber.e("Login failed ($responseCode): $responseMessage")
            ui { activity.toast("Login failed") }
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

        return Uri.parse("https://${BuildConfig.HOST}")
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
