package com.arnyminerz.filmagentaproto.ui.viewmodel

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.annotation.WorkerThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.arnyminerz.filmagentaproto.SyncWorker
import com.arnyminerz.filmagentaproto.account.Authenticator
import com.arnyminerz.filmagentaproto.account.credentials.Credentials
import com.arnyminerz.filmagentaproto.utils.async
import com.arnyminerz.filmagentaproto.utils.capitalized
import com.arnyminerz.filmagentaproto.utils.ui

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val am = AccountManager.get(application)

    val credentials = MutableLiveData<Credentials?>()

    @WorkerThread
    @Suppress("BlockingMethodInNonBlockingContext")
    fun addAccount(activity: Activity) = async {
        val credentials = credentials.value ?: throw IllegalStateException("Credentials not ready.")

        val name = credentials.name.lowercase().capitalized()
        val nif = credentials.nif
        val token = credentials.token

        val account = Account(name, Authenticator.AuthTokenType)
        am.addAccountExplicitly(account, nif, Bundle())
        am.setAuthToken(account, Authenticator.AuthTokenType, token)

        SyncWorker.run(activity).result.get()

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
