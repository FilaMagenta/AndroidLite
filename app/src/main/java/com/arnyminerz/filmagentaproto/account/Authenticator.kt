package com.arnyminerz.filmagentaproto.account

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.arnyminerz.filmagentaproto.activity.LoginActivity
import com.arnyminerz.filmagentaproto.exceptions.WrongCredentialsException
import timber.log.Timber

class Authenticator(private val context: Context) : AbstractAccountAuthenticator(context) {
    companion object {
        const val AuthTokenType = "com.arnyminerz.filmagentaproto"

        const val USER_DATA_VERSION = "version"
        const val USER_DATA_ID_SOCIO = "id_socio"

        const val VERSION = 1
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

        val version: Int? = am.getUserData(account, USER_DATA_VERSION)?.toIntOrNull()
        val idSocio: Long? = am.getUserData(account, USER_DATA_ID_SOCIO)?.toLongOrNull()
        if (version != VERSION) {
            // Old authentication, or version out of date, must authorize again
            Timber.w("Removing old account type for ${account.name}. Version: $version")
            am.removeAccountExplicitly(account)
        } else if (idSocio == null) {
            Timber.w("Removing account since it doesn't have a valid $USER_DATA_ID_SOCIO.")
            am.removeAccountExplicitly(account)
        } else {
            var token: String? = am.peekAuthToken(account, authTokenType)

            // If no token is found, try logging in with the stored password and dni
            if (token?.isNotEmpty() != true) {
                val dni = account.name
                val password: String? = am.getPassword(account)
                if (password != null)
                    try {
                        token = RemoteAuthentication.login(dni, password)
                    } catch (_: WrongCredentialsException) {
                        Timber.w("Got wrong credentials stored for ${account.name}.")
                    } catch (_: IllegalArgumentException) {
                        Timber.w("Credentials for ${account.name} are not available on the server.")
                    }
            }
            if (token?.isNotEmpty() == true)
                return Bundle().apply {
                    putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
                    putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
                    putString(AccountManager.KEY_AUTHTOKEN, token)
                }
        }

        // If this is reached, user must log in again
        val intent = Intent(context, LoginActivity::class.java).apply {
            putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            putExtra(LoginActivity.EXTRA_ACCOUNT_TYPE, account.type)
            putExtra(LoginActivity.EXTRA_AUTH_TOKEN_TYPE, authTokenType)
            putExtra(LoginActivity.EXTRA_DNI, account.name)
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