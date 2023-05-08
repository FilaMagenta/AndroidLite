package com.arnyminerz.filmagentaproto.utils

import android.accounts.AccountManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.arnyminerz.filmagentaproto.account.AccountHelper
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountHelperTest {
    companion object {
        const val TEST_DNI = "12345678X"
        const val TEST_PASSWORD = "password123"
        const val TEST_TOKEN = "auth-token-for-testing"
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
            am, TEST_DNI, TEST_PASSWORD, TEST_TOKEN, null
        )

        // Make sure the account was added
        assertNotNull(
            "The account was not added.",
            AccountHelper.getAccountsList(am).find { it.name == TEST_DNI }
        )

        // Remove the account created
        assertTrue(
            AccountHelper.removeAccount(am, TEST_DNI)
        )
    }

}