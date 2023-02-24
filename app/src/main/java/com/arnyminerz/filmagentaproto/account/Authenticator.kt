package com.arnyminerz.filmagentaproto.account

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.arnyminerz.filmagentaproto.activity.LoginActivity
import com.arnyminerz.filmagentaproto.database.remote.RemoteServer
import kotlinx.coroutines.runBlocking

class Authenticator(private val context: Context) : AbstractAccountAuthenticator(context) {
    companion object {
        const val AuthTokenType = "com.arnyminerz.filmagentaproto"
    }

    override fun addAccount(
        response: AccountAuthenticatorResponse?,
        accountType: String?,
        authTokenType: String?,
        requiredFeatures: Array<out String>?,
        options: Bundle?
    ): Bundle {
        val intent = Intent(context, LoginActivity::class.java).apply {
            putExtra(LoginActivity.EXTRA_ACCOUNT_TYPE, accountType)
            putExtra(LoginActivity.EXTRA_AUTH_TOKEN_TYPE, authTokenType)
            putExtra(LoginActivity.EXTRA_ADDING_NEW_ACCOUNT, true)
            putExtra(LoginActivity.EXTRA_RESPONSE, response)
        }
        return Bundle().apply {
            putParcelable(AccountManager.KEY_INTENT, intent)
        }
    }

    override fun getAuthToken(
        response: AccountAuthenticatorResponse?,
        account: Account,
        authTokenType: String?,
        options: Bundle?
    ): Bundle {
        val am = AccountManager.get(context)
        var token: String? = am.peekAuthToken(account, authTokenType)

        if (token == null || token.isEmpty()) {
            val password: String? = am.getPassword(account)
            if (password != null) runBlocking {
                token = try {
                    RemoteServer.login(account.name, password)
                } catch (e: Exception) {
                    Log.e("Authenticator", "Could not log in.", e)
                    null
                }
            }
        }
        if (token?.isNotEmpty() == true)
            return Bundle().apply {
                putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
                putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
                putString(AccountManager.KEY_AUTHTOKEN, token)
            }

        // If this is reached, user must log in again
        val intent = Intent(context, LoginActivity::class.java).apply {
            putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            putExtra(LoginActivity.EXTRA_ACCOUNT_TYPE, account.type)
            putExtra(LoginActivity.EXTRA_AUTH_TOKEN_TYPE, authTokenType)
        }
        return Bundle().apply {
            putParcelable(AccountManager.KEY_INTENT, intent)
        }
    }

    override fun editProperties(p0: AccountAuthenticatorResponse?, p1: String?): Bundle = Bundle()

    override fun confirmCredentials(
        p0: AccountAuthenticatorResponse?,
        p1: Account?,
        p2: Bundle?
    ): Bundle = Bundle()

    override fun getAuthTokenLabel(p0: String?): String {
        throw UnsupportedOperationException()
    }

    override fun hasFeatures(
        p0: AccountAuthenticatorResponse?,
        p1: Account?,
        p2: Array<out String>?
    ): Bundle {
        throw UnsupportedOperationException()
    }

    override fun updateCredentials(
        p0: AccountAuthenticatorResponse?,
        p1: Account?,
        p2: String?,
        p3: Bundle?
    ): Bundle {
        throw UnsupportedOperationException()
    }
}