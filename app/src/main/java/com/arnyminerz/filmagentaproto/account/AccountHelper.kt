package com.arnyminerz.filmagentaproto.account

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import com.arnyminerz.filamagenta.core.data.oauth.UserInformation
import com.arnyminerz.filamagenta.core.security.AccessToken
import com.arnyminerz.filmagentaproto.database.remote.RemoteDatabaseInterface
import com.arnyminerz.filmagentaproto.worker.SyncWorker

object AccountHelper {
    /**
     * Adds the given account data into a new account of the account manager.
     * @param am The account manager instance to use.
     * @param dni The DNI of the new user.
     * @param token The token for performing interactions as the user.
     * @param context If not null, runs synchronization once the account is added.
     * @throws IllegalStateException If an account with the given DNI already exists.
     * @throws RuntimeException If there's an exception while adding the account.
     */
    fun addAccount(
        am: AccountManager,
        userInformation: UserInformation,
        token: AccessToken,
        context: Context?
    ) {
        val dni = userInformation.login
        val idSocio = RemoteDatabaseInterface.fetchIdSocioFromDni(dni)

        // Check that the account doesn't already exist
        val accounts = am.getAccountsByType(Authenticator.AccountType)
        if (accounts.find { am.getUserData(it, Authenticator.USER_DATA_DNI) == dni } != null)
            throw IllegalStateException("An account with DNI $dni already exists.")

        val account = Account("${userInformation.displayName} ($dni)", Authenticator.AccountType)
        val added = am.addAccountExplicitly(account, dni, Bundle())
        if (!added) throw RuntimeException("There was an error while adding the account.")

        am.setAuthToken(account, Authenticator.AuthTokenType, token.token)
        am.setUserData(account, Authenticator.USER_DATA_VERSION, Authenticator.VERSION.toString())
        am.setUserData(account, Authenticator.USER_DATA_ID_SOCIO, idSocio.toString())
        am.setUserData(account, Authenticator.USER_DATA_REFRESH_TOKEN, token.refreshToken)
        am.setUserData(account, Authenticator.USER_DATA_TOKEN_EXPIRATION, token.expiration.toEpochMilli().toString())
        am.setUserData(account, Authenticator.USER_DATA_DNI, userInformation.login)
        am.setUserData(account, Authenticator.USER_DATA_DISPLAY_NAME, userInformation.displayName)
        am.setUserData(account, Authenticator.USER_DATA_EMAIL, userInformation.email)

        // If context is given, run synchronization
        if (context != null) SyncWorker.run(context)
    }

    /**
     * Removes the account with the given DNI from the account manager.
     * @param am The account manager instance to use.
     * @param dni The DNI of the user to remove.
     * @return `true` if the account was deleted successfully, `false` if it didn't exist or
     * couldn't be removed.
     */
    fun removeAccount(am: AccountManager, dni: String): Boolean {
        val account = Account(dni, Authenticator.AccountType)

        return am.removeAccountExplicitly(account)
    }

    /**
     * Returns a list with all the accounts currently added.
     */
    fun getAccountsList(am: AccountManager): Array<Account> = am.getAccountsByType(Authenticator.AccountType)
}
