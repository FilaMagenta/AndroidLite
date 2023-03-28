package com.arnyminerz.filmagentaproto.ui.viewmodel

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.annotation.WorkerThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.arnyminerz.filmagentaproto.account.Authenticator
import com.arnyminerz.filmagentaproto.account.credentials.Credentials
import com.arnyminerz.filmagentaproto.database.remote.RemoteDatabaseInterface
import com.arnyminerz.filmagentaproto.utils.async
import com.arnyminerz.filmagentaproto.utils.ui
import com.arnyminerz.filmagentaproto.worker.SyncWorker

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val am = AccountManager.get(application)

    val credentials = MutableLiveData<Credentials?>()

    @WorkerThread
    @Suppress("BlockingMethodInNonBlockingContext")
    fun addAccount(activity: Activity) = async {
        val credentials = credentials.value ?: throw IllegalStateException("Credentials not ready.")

        val dni = credentials.nif.uppercase()
        val password = credentials.password
        val token = credentials.token

        val idSocio = RemoteDatabaseInterface.fetchIdSocioFromDni(dni)

        val account = Account(dni, Authenticator.AuthTokenType)
        am.addAccountExplicitly(account, password, Bundle())
        am.setAuthToken(account, Authenticator.AuthTokenType, token)
        am.setUserData(account, Authenticator.USER_DATA_VERSION, Authenticator.VERSION.toString())
        am.setUserData(account, Authenticator.USER_DATA_ID_SOCIO, idSocio.toString())

        SyncWorker.run(activity)

        ui {
            activity.setResult(Activity.RESULT_OK)
            activity.finish()
        }
    }

    /**
     * Returns all the accounts the application has registered.
     */
    fun getAccounts(): Array<out Account> = am.getAccountsByType(Authenticator.AuthTokenType)
}
