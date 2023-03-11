package com.arnyminerz.filmagentaproto.ui.viewmodel

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.arnyminerz.filmagentaproto.SyncWorker
import com.arnyminerz.filmagentaproto.account.Authenticator
import com.arnyminerz.filmagentaproto.account.credentials.Credentials
import com.arnyminerz.filmagentaproto.account.credentials.WCCredentials
import com.arnyminerz.filmagentaproto.utils.async
import com.arnyminerz.filmagentaproto.utils.capitalized
import com.arnyminerz.filmagentaproto.utils.ui
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import org.json.JSONException
import org.json.JSONObject

private const val TAG = "LoginViewModel"

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val am = AccountManager.get(application)

    private val client = HttpClient {
        install(WebSockets)
    }

    private var listening = false

    val credentials = MutableLiveData<Credentials?>()
    val wcCredentials = MutableLiveData<WCCredentials?>()

    fun listen(): Job? = if (!listening)
        async {
            Log.d(TAG, "Starting WS listener...")
            client.webSocket(method = HttpMethod.Get, host = "wsrelay-ws.arnyminerz.com") {
                listening = true
                Log.d(TAG, "Started listening WS")
                while (listening) {
                    try {
                        val message = incoming.receive() as? Frame.Text ?: continue
                        Log.d(TAG, "Received data from server WS.")
                        try {
                            val text = message.readText()
                            val json = JSONObject(text)
                            val body = json.getJSONObject("body")
                            val credentials = WCCredentials.fromJSON(body)
                            Log.i(TAG, "Got WC credentials, updating...")
                            wcCredentials.postValue(credentials)
                        } catch (e: JSONException) {
                            Log.e(TAG, "Could not parse JSON response.")
                        }
                    } catch (e: ClosedReceiveChannelException) {
                        Log.i(TAG, "Listening again, connection closed.")
                        listening = false
                        client.close()
                        listen()
                    }
                }
            }
            client.close()
        }
    else null

    override fun onCleared() {
        listening = false
    }

    @WorkerThread
    @Suppress("BlockingMethodInNonBlockingContext")
    fun addAccount(activity: Activity) = async {
        val credentials = credentials.value ?: throw IllegalStateException("Credentials not ready.")
        val wcCredentials = wcCredentials.value ?: throw IllegalStateException("WCCredentials not ready.")

        val name = credentials.name.lowercase().capitalized()
        val nif = credentials.nif
        val token = credentials.token

        val account = Account(name, Authenticator.AuthTokenType)
        am.addAccountExplicitly(account, nif, Bundle())
        am.setAuthToken(account, Authenticator.AuthTokenType, token)
        am.setUserData(account, "key_id", wcCredentials.keyId.toString())
        am.setUserData(account, "consumer_key", wcCredentials.consumerKey)
        am.setUserData(account, "consumer_secret", wcCredentials.consumerSecret)

        SyncWorker.run(activity).result.get()

        ui {
            activity.setResult(Activity.RESULT_OK)
            activity.finish()
        }
    }
}
