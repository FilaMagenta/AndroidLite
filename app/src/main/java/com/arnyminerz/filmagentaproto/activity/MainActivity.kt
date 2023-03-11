package com.arnyminerz.filmagentaproto.activity

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.OnAccountsUpdateListener
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.core.os.HandlerCompat
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.work.WorkInfo
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.SyncWorker
import com.arnyminerz.filmagentaproto.account.Authenticator
import com.arnyminerz.filmagentaproto.database.data.PersonalData
import com.arnyminerz.filmagentaproto.database.local.AppDatabase
import com.arnyminerz.filmagentaproto.database.remote.protos.Socio
import com.arnyminerz.filmagentaproto.storage.SELECTED_ACCOUNT
import com.arnyminerz.filmagentaproto.storage.dataStore
import com.arnyminerz.filmagentaproto.ui.components.ErrorCard
import com.arnyminerz.filmagentaproto.ui.components.LoadingBox
import com.arnyminerz.filmagentaproto.ui.components.NavigationBarItem
import com.arnyminerz.filmagentaproto.ui.components.NavigationBarItems
import com.arnyminerz.filmagentaproto.ui.components.ProfileImage
import com.arnyminerz.filmagentaproto.ui.dialogs.AccountsDialog
import com.arnyminerz.filmagentaproto.ui.screens.MainPage
import com.arnyminerz.filmagentaproto.ui.screens.ProfilePage
import com.arnyminerz.filmagentaproto.ui.screens.SettingsScreen
import com.arnyminerz.filmagentaproto.ui.theme.setContentThemed
import com.arnyminerz.filmagentaproto.utils.LaunchedEffectFlow
import com.arnyminerz.filmagentaproto.utils.async
import com.arnyminerz.filmagentaproto.utils.doAsync
import com.arnyminerz.filmagentaproto.utils.trimmedAndCaps
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTextApi::class, ExperimentalPagerApi::class)
class MainActivity : AppCompatActivity(), OnAccountsUpdateListener {
    companion object {
        val TOP_BAR_HEIGHT = (56 + 16).dp

        private const val TAG = "MainActivity"
    }

    private val viewModel by viewModels<MainViewModel>()

    private lateinit var am: AccountManager

    private lateinit var loginRequestLauncher: ActivityResultLauncher<LoginActivity.Contract.Data>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        am = AccountManager.get(this)
        loginRequestLauncher = registerForActivityResult(LoginActivity.Contract()) { loggedIn ->
            val accounts = am.getAccountsByType(Authenticator.AuthTokenType)
            if (!loggedIn && accounts.isEmpty())
                finish()
        }

        setContentThemed {
            val selectedAccountIndex by viewModel.selectedAccount.collectAsState(null)
            val accounts by viewModel.accounts.observeAsState(emptyArray())

            var showingAccountsDialog by remember { mutableStateOf(false) }
            if (showingAccountsDialog)
                AccountsDialog(
                    accountsList = accounts,
                    selectedAccountIndex = selectedAccountIndex ?: -1,
                    onAccountSelected = { index, _ ->
                        Log.i(TAG, "Switching account to #$index")
                        doAsync {
                            dataStore.edit {
                                it[SELECTED_ACCOUNT] = index
                                showingAccountsDialog = false
                            }
                        }
                    },
                    onNewAccountRequested = {
                        loginRequestLauncher.launch(LoginActivity.Contract.Data(true, null))
                    },
                    onAccountRemoved = { am.removeAccountExplicitly(it) },
                    onDismissRequested = { showingAccountsDialog = false },
                )

            var currentPage by remember { mutableStateOf(0) }

            selectedAccountIndex?.let { accountIndex ->
                Scaffold(
                    topBar = {
                        AnimatedVisibility(
                            visible = currentPage == 0,
                            enter = slideInVertically(tween(durationMillis = 300)) { -it },
                            exit = slideOutVertically(tween(durationMillis = 300)) { -it },
                        ) {
                            CenterAlignedTopAppBar(
                                title = { Text(stringResource(R.string.app_name)) },
                                actions = {
                                    accounts
                                        .takeIf { it.isNotEmpty() }
                                        ?.getOrNull(accountIndex)
                                        ?.let {
                                            ProfileImage(
                                                name = it.name.uppercase(),
                                                modifier = Modifier
                                                    .clip(CircleShape)
                                                    .clickable { showingAccountsDialog = true },
                                            )
                                        }
                                        ?: doAsync {
                                            if (selectedAccountIndex != 0)
                                                dataStore.edit {
                                                    it[SELECTED_ACCOUNT] = 0
                                                }
                                        }
                                },
                            )
                        }
                    },
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItems(
                                selectedIndex = currentPage,
                                onSelected = { currentPage = it },
                                items = listOf(
                                    NavigationBarItem(
                                        Icons.Rounded.Wallet,
                                        Icons.Outlined.Wallet,
                                        R.string.navigation_balance,
                                    ),
                                    NavigationBarItem(
                                        Icons.Rounded.Person,
                                        Icons.Outlined.Person,
                                        R.string.navigation_profile,
                                    ),
                                    NavigationBarItem(
                                        Icons.Rounded.Settings,
                                        Icons.Outlined.Settings,
                                        R.string.navigation_settings,
                                    ),
                                ),
                            )
                        }
                    },
                ) { paddingValues ->
                    val personalData by viewModel.personalData.observeAsState()
                    val selectedAccount = accounts.getOrNull(accountIndex)
                    val databaseData by viewModel.databaseData.observeAsState(emptyList())
                    val topPadding by animateDpAsState(
                        if (currentPage == 0)
                            TOP_BAR_HEIGHT
                        else
                            0.dp,
                        animationSpec = tween(durationMillis = 300),
                    )

                    LaunchedEffectFlow(selectedAccountIndex ?: -1, { it }) { index ->
                        if (index < 0) return@LaunchedEffectFlow
                        val account = accounts.getOrNull(index) ?: return@LaunchedEffectFlow
                        val password: String? = am.getPassword(account)
                        val dni = password?.trimmedAndCaps
                        val socio = databaseData.find { it.Dni?.trimmedAndCaps == dni }
                            ?: return@LaunchedEffectFlow
                        viewModel.getAssociatedAccounts(socio.idSocio)
                    }

                    selectedAccount
                        ?.let { account ->
                            val data =
                                personalData?.find { it.accountName == account.name && it.accountType == account.type }
                            data?.let { account to it }
                        }
                        ?.let { (account, data) ->
                            val dni = am.getPassword(account).trimmedAndCaps
                            data to databaseData.find { it.Dni?.trimmedAndCaps == dni }
                        }
                        ?.let { (data, socio) ->
                            val pagerState = rememberPagerState()

                            LaunchedEffectFlow(pagerState, { it.currentPage }) { currentPage = it }
                            LaunchedEffectFlow(currentPage, { it }) { pagerState.scrollToPage(it) }

                            HorizontalPager(
                                count = 3,
                                state = pagerState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(
                                        top = topPadding,
                                        bottom = paddingValues.calculateBottomPadding()
                                    ),
                            ) { page ->
                                when (page) {
                                    0 -> MainPage(data, viewModel)
                                    1 -> socio?.let { socio ->
                                        ProfilePage(socio, accounts) { _, index ->
                                            doAsync {
                                                dataStore.edit { it[SELECTED_ACCOUNT] = index }
                                            }
                                        }
                                    } ?: ErrorCard(stringResource(R.string.error_find_data))
                                    2 -> SettingsScreen()
                                }
                            }
                        }
                        ?: LoadingBox()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        am.addOnAccountsUpdatedListener(this, HandlerCompat.createAsync(mainLooper), true)

        val accounts = am.getAccountsByType(Authenticator.AuthTokenType)
        if (accounts.isEmpty())
            loginRequestLauncher.launch(
                LoginActivity.Contract.Data(true, null)
            )
    }

    override fun onPause() {
        super.onPause()
        am.removeOnAccountsUpdatedListener(this)
    }

    override fun onAccountsUpdated(accounts: Array<out Account>?) {
        viewModel.accounts.postValue(accounts)
    }

    class MainViewModel(application: Application) : AndroidViewModel(application) {
        private val database = AppDatabase.getInstance(application)
        private val personalDataDao = database.personalDataDao()
        private val remoteDatabaseDao = database.remoteDatabaseDao()

        val selectedAccount = application
            .dataStore
            .data
            .map { preferences -> preferences[SELECTED_ACCOUNT] ?: 0 }

        val accounts: MutableLiveData<Array<out Account>> = MutableLiveData()

        val personalData = personalDataDao.getAllLive()

        val databaseData = remoteDatabaseDao.getAllLive()

        val isLoading = Transformations.map(SyncWorker.getLiveState(application)) { list ->
            list.any { it.state == WorkInfo.State.RUNNING }
        }

        val associatedAccounts = MutableLiveData<List<Pair<Socio, PersonalData?>>>()

        fun getAssociatedAccounts(associatedWithId: Int) = async {
            val socios = remoteDatabaseDao.getAllAssociatedWith(associatedWithId)
            val personalDataList = personalDataDao.getAll()
            val accounts =
                socios.map { socio -> socio to personalDataList.find { it.name == socio.Nombre } }
            Log.i(TAG, "Got ${accounts.size} associated accounts for #$associatedWithId")
            associatedAccounts.postValue(accounts)
        }
    }
}
