package com.arnyminerz.filmagentaproto.activity

import android.accounts.AccountAuthenticatorResponse
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.account.Authenticator.Companion.AuthTokenType
import com.arnyminerz.filmagentaproto.account.credentials.Credentials
import com.arnyminerz.filmagentaproto.ui.screens.LoginScreen
import com.arnyminerz.filmagentaproto.ui.theme.setContentThemed
import com.arnyminerz.filmagentaproto.ui.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
class LoginActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_ACCOUNT_TYPE = "account_type"
        const val EXTRA_AUTH_TOKEN_TYPE = "auth_token_type"
        const val EXTRA_ADDING_NEW_ACCOUNT = "adding_new_account"
        const val EXTRA_RESPONSE = "response"
    }

    private val viewModel by viewModels<LoginViewModel>()

    class Contract : ActivityResultContract<Contract.Data, Boolean>() {
        data class Data(
            val addingNewAccount: Boolean,
            val response: AccountAuthenticatorResponse?,
        )

        override fun createIntent(context: Context, input: Data): Intent =
            Intent(context, LoginActivity::class.java).apply {
                putExtra(EXTRA_ACCOUNT_TYPE, AuthTokenType)
                putExtra(EXTRA_AUTH_TOKEN_TYPE, AuthTokenType)
                putExtra(EXTRA_ADDING_NEW_ACCOUNT, input.addingNewAccount)
                putExtra(EXTRA_RESPONSE, input.response)
            }

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
            resultCode == Activity.RESULT_OK
    }

    private lateinit var accountType: String
    private lateinit var authTokenType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        accountType = intent.getStringExtra(EXTRA_ACCOUNT_TYPE)!!
        authTokenType = intent.getStringExtra(EXTRA_AUTH_TOKEN_TYPE)!!

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val accounts = viewModel.getAccounts()
                if (accounts.isEmpty())
                    finishAndRemoveTask()
                else {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
        })

        setContentThemed {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(stringResource(R.string.login_title)) },
                        navigationIcon = {
                            IconButton(
                                onClick = { setResult(Activity.RESULT_CANCELED); finish() },
                            ) {
                                Icon(Icons.Rounded.ChevronLeft, stringResource(R.string.back))
                            }
                        },
                    )
                }
            ) { paddingValues ->
                val credentials by viewModel.credentials.observeAsState()

                LaunchedEffect(credentials) {
                    snapshotFlow { credentials }
                        .collect {
                            // If credentials have been stored, move to the next page
                            if (it != null)
                                viewModel.addAccount(this@LoginActivity)
                        }
                }

                LoginScreen(Modifier.padding(paddingValues)) { name, nif, token ->
                    // Store the returned credentials
                    viewModel.credentials.postValue(Credentials(name, nif, token))
                }
            }
        }
    }
}