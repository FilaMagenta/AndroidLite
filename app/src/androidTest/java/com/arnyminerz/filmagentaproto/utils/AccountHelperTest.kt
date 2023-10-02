package com.arnyminerz.filmagentaproto.utils

import android.accounts.AccountManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.arnyminerz.filamagenta.core.data.oauth.UserInformation
import com.arnyminerz.filamagenta.core.security.AccessToken
import com.arnyminerz.filmagentaproto.account.AccountHelper
import com.arnyminerz.filmagentaproto.account.Authenticator.Companion.USER_DATA_DNI
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime

class AccountHelperTest {
    companion object {
        const val TEST_DNI = "12345678X"

        val testUserInformation = UserInformation(
            0, "", "", "", LocalDateTime.now(), 0, "", emptyList()
        )

        val testAccessToken = AccessToken("", Instant.now(), Int.MAX_VALUE, "")
    }

    private val context: Context by lazy {
        ApplicationProvider.getApplicationContext()
    }

    private val am: AccountManager by lazy {
        AccountManager.get(context)
    }

    @Test
    fun testAdditionAndRemoval() {
        // Try adding the account without synchronization
        AccountHelper.addAccount(
            am, testUserInformation, testAccessToken, null
        )

        // Make sure the account was added
        assertNotNull(
            "The account was not added.",
            AccountHelper.getAccountsList(am).find { am.getUserData(it, USER_DATA_DNI) == TEST_DNI }
        )

        // Remove the account created
        assertTrue(
            AccountHelper.removeAccount(am, TEST_DNI)
        )
    }

}