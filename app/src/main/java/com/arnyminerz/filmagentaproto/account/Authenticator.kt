package com.arnyminerz.filmagentaproto.account

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.arnyminerz.filamagenta.core.exception.WrongCredentialsException
import com.arnyminerz.filamagenta.core.remote.RemoteAuthentication
import com.arnyminerz.filamagenta.core.remote.result.auth.TokenResult
import com.arnyminerz.filmagentaproto.BuildConfig
import com.arnyminerz.filmagentaproto.activity.LoginActivity
import com.arnyminerz.filmagentaproto.database.remote.RemoteDatabaseInterface
import timber.log.Timber
import java.time.Instant

class Authenticator(private val context: Context) : AbstractAccountAuthenticator(context) {
    companion object {
        const val AuthTokenType = "com.arnyminerz.filmagentaproto"
        const val AccountType = AuthTokenType

        const val USER_DATA_VERSION = "version"
        const val USER_DATA_ID_SOCIO = "id_socio"
        const val USER_DATA_REFRESH_TOKEN = "refresh_token"
        const val USER_DATA_TOKEN_EXPIRATION = "token_expiration"

        const val USER_DATA_CUSTOMER_ID = "customer_id"
        const val USER_DATA_CUSTOMER_ADMIN = "customer_admin"

        const val USER_DATA_DNI = "dni"
        const val USER_DATA_DISPLAY_NAME = "display_name"
        const val USER_DATA_EMAIL = "email"

        const val VERSION = 2

        val ERROR_DNI_NOT_FOUND = 1 to "A socio with the account's DNI was not found."
    }

    private val am = AccountManager.get(context)

    private val remoteAuthentication by lazy {
        RemoteAuthentication.getInstance(
            BuildConfig.HOST,
            BuildConfig.OAUTH_CLIENT_ID,
            BuildConfig.OAUTH_CLIENT_SECRET
        )
    }

    private fun Bundle.putError(error: Pair<Int, String>) {
        val (code, message) = error
        putInt(AccountManager.KEY_ERROR_CODE, code)
        putString(AccountManager.KEY_ERROR_MESSAGE, message)
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
            val refreshToken: String? = am.getUserData(account, USER_DATA_REFRESH_TOKEN)
            val tokenExpiration: Instant? = am.getUserData(account, USER_DATA_TOKEN_EXPIRATION)
                ?.toLong()
                ?.let(Instant::ofEpochMilli)
            val now = Instant.now()

            if (refreshToken != null && (tokenExpiration == null || tokenExpiration > now)) {
                // token has expired, refresh it
                val refreshedToken = remoteAuthentication.refreshToken(refreshToken)
                if (refreshedToken is TokenResult.Success) {
                    AccountHelper.updateAccountToken(am, account, refreshedToken.token)
                    token = refreshedToken.token.token
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

    override fun editProperties(p0: AccountAuthenticatorResponse?, p1: String?): Bundle {
        throw UnsupportedOperationException()
    }

    override fun confirmCredentials(
        p0: AccountAuthenticatorResponse?,
        p1: Account?,
        p2: Bundle?
    ): Bundle {
        throw UnsupportedOperationException()
    }

    override fun getAuthTokenLabel(p0: String?): String {
        throw UnsupportedOperationException()
    }

    override fun hasFeatures(
        p0: AccountAuthenticatorResponse?,
        p1: Account?,
        p2: Array<out String>?
    ): Bundle = Bundle().apply {
        putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false)
    }

    override fun isCredentialsUpdateSuggested(
        response: AccountAuthenticatorResponse,
        account: Account,
        statusToken: String?
    ): Bundle = Bundle().apply {
        val version = am.getUserData(account, USER_DATA_VERSION)?.toLongOrNull()
        if (version == null || version < VERSION) {
            Timber.i("Account (${account.name}) doesn't have a version, or it's outdated ($version). Should update credentials.")
            putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true)
            return@apply
        }
        val idSocio = am.getUserData(account, USER_DATA_ID_SOCIO)?.toLongOrNull()
        if (idSocio == null) {
            Timber.i("Account (${account.name}) doesn't contain the idSocio in its data. Should update credentials.")
            putBoolean(AccountManager.KEY_BOOLEAN_RESULT, true)
            return@apply
        }
        putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false)
    }

    override fun updateCredentials(
        response: AccountAuthenticatorResponse,
        account: Account,
        authTokenType: String?,
        options: Bundle?
    ): Bundle = Bundle().apply {
        val dni = account.name
        val idSocio = RemoteDatabaseInterface.fetchIdSocioFromDni(dni)
        if (idSocio == null) {
            putError(ERROR_DNI_NOT_FOUND)
            return@apply
        }

        am.setUserData(account, USER_DATA_VERSION, VERSION.toString())
        am.setUserData(account, USER_DATA_ID_SOCIO, idSocio.toString())

        putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
        putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
    }
}