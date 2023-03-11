package com.arnyminerz.filmagentaproto.activity

import android.accounts.AccountAuthenticatorResponse
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.arnyminerz.filmagentaproto.BuildConfig
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.account.Authenticator.Companion.AuthTokenType
import com.arnyminerz.filmagentaproto.account.credentials.Credentials
import com.arnyminerz.filmagentaproto.ui.components.WebView
import com.arnyminerz.filmagentaproto.ui.screens.LoginScreen
import com.arnyminerz.filmagentaproto.ui.theme.setContentThemed
import com.arnyminerz.filmagentaproto.ui.viewmodel.LoginViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

private const val TAG = "LoginActivity"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class, ExperimentalPagerApi::class)
class LoginActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_ACCOUNT_TYPE = "account_type"
        const val EXTRA_AUTH_TOKEN_TYPE = "auth_token_type"
        const val EXTRA_ADDING_NEW_ACCOUNT = "adding_new_account"
        const val EXTRA_RESPONSE = "response"

        private val AUTHORIZE_QUERY = mapOf(
            "app_name" to "Filà Magenta",
            "scope" to "read_write",
            "return_url" to "https://filamagenta.com",
            "callback_url" to "https://wsrelay.arnyminerz.com",
            "user_id" to "123",
        )

        private const val AUTHORIZE_URL = "https://${BuildConfig.HOST}/wc-auth/v1/authorize"

        /**
         * The address to be used for authorizing new users.
         */
        private val AUTHORIZE_ADDRESS = Uri.parse(AUTHORIZE_URL)
            .buildUpon()
            .apply {
                for ((key, value) in AUTHORIZE_QUERY)
                    appendQueryParameter(key, value)
            }
            .build()
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

        viewModel.listen()

        setContentThemed {
            var loading by remember { mutableStateOf(false) }

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
                        actions = {
                            AnimatedVisibility(visible = loading) {
                                CircularProgressIndicator()
                            }
                        },
                    )
                }
            ) { paddingValues ->
                val scope = rememberCoroutineScope()
                val pagerState = rememberPagerState()

                val credentials by viewModel.credentials.observeAsState()
                val wcCredentials by viewModel.wcCredentials.observeAsState()

                LaunchedEffect(credentials) {
                    snapshotFlow { credentials }
                        .collect {
                            // If credentials have been stored, move to the next page
                            if (it != null)
                                scope.launch { pagerState.animateScrollToPage(1) }
                        }
                }
                LaunchedEffect(wcCredentials) {
                    snapshotFlow { wcCredentials }
                        .collect {
                            // If credentials have been stored, login process is complete, create
                            // the new account
                            if (it != null)
                                viewModel.addAccount(this@LoginActivity)
                        }
                }

                HorizontalPager(
                    count = 2,
                    state = pagerState,
                    userScrollEnabled = false,
                    modifier = Modifier
                        .padding(paddingValues)
                        .imePadding(),
                ) { page ->
                    when(page) {
                        0 -> LoginScreen { name, nif, token ->
                            // Store the returned credentials
                            viewModel.credentials.postValue(Credentials(name, nif, token))
                        }
                        1 -> WebView(
                            url = AUTHORIZE_ADDRESS.toString(),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            loadProgressReceiver = { _, _, finished ->
                                loading = !finished
                            },
                        )
                    }
                }
            }
        }
    }
}