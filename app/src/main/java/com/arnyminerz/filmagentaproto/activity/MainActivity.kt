package com.arnyminerz.filmagentaproto.activity

import android.Manifest
import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.OnAccountsUpdateListener
import android.app.Application
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsIntent.SHARE_STATE_OFF
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.os.HandlerCompat
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.work.WorkInfo
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.SyncWorker
import com.arnyminerz.filmagentaproto.account.Authenticator
import com.arnyminerz.filmagentaproto.database.data.PersonalData
import com.arnyminerz.filmagentaproto.database.data.woo.Customer
import com.arnyminerz.filmagentaproto.database.data.woo.Event
import com.arnyminerz.filmagentaproto.database.data.woo.Order
import com.arnyminerz.filmagentaproto.database.local.AppDatabase
import com.arnyminerz.filmagentaproto.database.logic.isConfirmed
import com.arnyminerz.filmagentaproto.database.remote.RemoteCommerce
import com.arnyminerz.filmagentaproto.database.remote.protos.Socio
import com.arnyminerz.filmagentaproto.storage.SELECTED_ACCOUNT
import com.arnyminerz.filmagentaproto.storage.dataStore
import com.arnyminerz.filmagentaproto.ui.components.ErrorCard
import com.arnyminerz.filmagentaproto.ui.components.LoadingBox
import com.arnyminerz.filmagentaproto.ui.components.NavigationBarItem
import com.arnyminerz.filmagentaproto.ui.components.NavigationBarItems
import com.arnyminerz.filmagentaproto.ui.components.ProfileImage
import com.arnyminerz.filmagentaproto.ui.dialogs.AccountsDialog
import com.arnyminerz.filmagentaproto.ui.dialogs.PaymentBottomSheet
import com.arnyminerz.filmagentaproto.ui.screens.EventsScreen
import com.arnyminerz.filmagentaproto.ui.screens.InitialLoadScreen
import com.arnyminerz.filmagentaproto.ui.screens.MainPage
import com.arnyminerz.filmagentaproto.ui.screens.ProfilePage
import com.arnyminerz.filmagentaproto.ui.screens.SettingsScreen
import com.arnyminerz.filmagentaproto.ui.theme.setContentThemed
import com.arnyminerz.filmagentaproto.utils.LaunchedEffectFlow
import com.arnyminerz.filmagentaproto.utils.async
import com.arnyminerz.filmagentaproto.utils.doAsync
import com.arnyminerz.filmagentaproto.utils.io
import com.arnyminerz.filmagentaproto.utils.toast
import com.arnyminerz.filmagentaproto.utils.trimmedAndCaps
import com.arnyminerz.filmagentaproto.utils.ui
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@OptIn(
    ExperimentalTextApi::class,
    ExperimentalPagerApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
class MainActivity : AppCompatActivity(), OnAccountsUpdateListener {
    companion object {
        val TOP_BAR_HEIGHT = (56 + 16).dp

        private const val TAG = "MainActivity"
    }

    private val viewModel by viewModels<MainViewModel>()

    private lateinit var am: AccountManager

    private lateinit var loginRequestLauncher: ActivityResultLauncher<LoginActivity.Contract.Data>

    private val notificationPermissionRequestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (!granted) toast(R.string.error_toast_notifications)
    }

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

            val databaseData by viewModel.databaseData.observeAsState()

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

            val processingPayment by viewModel.processingPayment.observeAsState(false)
            var showingPaymentBottomSheet by remember { mutableStateOf(false) }
            if (showingPaymentBottomSheet)
                PaymentBottomSheet(
                    isLoading = processingPayment,
                    onPaymentRequested = { amount, concept ->
                        viewModel.makePayment(amount, concept) { paymentUrl ->
                            showingPaymentBottomSheet = false
                            val intent = CustomTabsIntent.Builder()
                                .setShareState(SHARE_STATE_OFF)
                                .build()
                            intent.launchUrl(this, Uri.parse(paymentUrl))
                        }
                    },
                    onDismissRequest = { showingPaymentBottomSheet = false },
                )

            var currentPage by remember { mutableStateOf(0) }

            if (databaseData?.isEmpty() == true)
                return@setContentThemed InitialLoadScreen()

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
                                        Icons.Rounded.CalendarMonth,
                                        Icons.Outlined.CalendarMonth,
                                        R.string.navigation_events,
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
                    floatingActionButton = {
                        FloatingActionButton(onClick = { showingPaymentBottomSheet = true }) {
                            Icon(Icons.Rounded.Wallet, "")
                        }
                    },
                ) { paddingValues ->
                    val personalData by viewModel.personalData.observeAsState()
                    val selectedAccount = accounts.getOrNull(accountIndex)
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
                        val socio = databaseData?.find { it.Dni?.trimmedAndCaps == dni }
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
                            data to databaseData?.find { it.Dni?.trimmedAndCaps == dni }
                        }
                        ?.let { (data, socio) ->
                            val pagerState = rememberPagerState()

                            LaunchedEffectFlow(pagerState, { it.currentPage }) {
                                currentPage = it
                            }
                            LaunchedEffectFlow(
                                currentPage,
                                { it }) { pagerState.scrollToPage(it) }

                            HorizontalPager(
                                count = 4,
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
                                    1 -> EventsScreen(viewModel)
                                    2 -> socio?.let { socio ->
                                        ProfilePage(socio, accounts) { _, index ->
                                            doAsync {
                                                dataStore.edit { it[SELECTED_ACCOUNT] = index }
                                            }
                                        }
                                    } ?: ErrorCard(stringResource(R.string.error_find_data))
                                    3 -> SettingsScreen()
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) notificationPermissionRequestLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
        private val wooCommerceDao = database.wooCommerceDao()

        private val am = AccountManager.get(application)

        val selectedAccount = application
            .dataStore
            .data
            .map { preferences -> preferences[SELECTED_ACCOUNT] ?: 0 }

        val accounts: MutableLiveData<Array<out Account>> = MutableLiveData()

        val personalData = personalDataDao.getAllLive()

        val databaseData = remoteDatabaseDao.getAllLive()

        val workerState = SyncWorker.getLiveState(application)

        val isLoading = workerState.map { list ->
            list.any { it.state == WorkInfo.State.RUNNING }
        }

        val associatedAccounts = MutableLiveData<List<Pair<Socio, PersonalData?>>>()

        val customer = selectedAccount
            .map { index ->
                accounts.value?.get(index)?.let { account ->
                    val customerId: Long = am.getUserData(account, "customer_id")
                        ?.toLongOrNull() ?: return@let null
                    wooCommerceDao.getAllCustomers().find { it.id == customerId }
                }
            }

        val events = wooCommerceDao.getAllEventsLive()

        val orders = wooCommerceDao.getAllOrdersLive()

        val confirmedEvents = MutableLiveData<List<Event>>()
        val availableEvents = MutableLiveData<List<Event>>()

        val processingPayment = MutableLiveData(false)

        fun getAssociatedAccounts(associatedWithId: Int) = async {
            val socios = remoteDatabaseDao.getAllAssociatedWith(associatedWithId)
            val personalDataList = personalDataDao.getAll()
            val accounts =
                socios.map { socio -> socio to personalDataList.find { it.name == socio.Nombre } }
            Log.i(TAG, "Got ${accounts.size} associated accounts for #$associatedWithId")
            associatedAccounts.postValue(accounts)
        }

        private suspend fun isConfirmed(event: Event, customer: Customer?): Boolean {
            if (customer == null) return false
            return event.isConfirmed(getApplication(), customer)
        }

        suspend fun updateConfirmedEvents(customer: Customer?) {
            if (customer == null) return
            val events = events.value ?: return
            val (confirmed, available) = io {
                val confirmed = mutableMapOf<Event, Boolean>()
                events.forEach { event ->
                    confirmed[event] = isConfirmed(event, customer)
                }
                val confirmedEvents = confirmed.filter { it.value }.keys.toList()
                val availableEvents = confirmed.filter { !it.value }.keys.toList()
                confirmedEvents to availableEvents
            }
            confirmedEvents.postValue(confirmed)
            availableEvents.postValue(available)
        }

        fun makePayment(
            amount: Double,
            concept: String,
            onComplete: (paymentUrl: String) -> Unit
        ) = async {
            val paymentUrl = try {
                processingPayment.postValue(true)
                Log.d(TAG, "Requesting a payment of $amount €. Getting customer...")
                val customer = customer.first()
                    ?: throw IllegalStateException("Could not get current customer.")
                Log.d(TAG, "Customer ID for payment: ${customer.id}")
                val payments = wooCommerceDao.getAllAvailablePayments()
                Log.d(TAG, "Making request...")
                val url = RemoteCommerce.transferAmount(amount, concept, payments, customer)
                Log.i(TAG, "Payment url: $url")
                url
            } catch (e: Exception) {
                Log.e(TAG, "Could not make payment: ${e.message}")
                null
            } finally {
                processingPayment.postValue(false)
            } ?: return@async
            ui {
                onComplete(paymentUrl)
            }
        }

        @Suppress("BlockingMethodInNonBlockingContext")
        fun signUpForEvent(customer: Customer, event: Event, metadata: List<Order.Metadata>) =
            async {
                Log.i(TAG, "Signing up for event. Metadata: $metadata")
                RemoteCommerce.eventSignup(
                    customer,
                    "", // FIXME: Set notes
                    event = event,
                    metadata = metadata,
                )
                Log.i(TAG, "Confirmed sign up. Syncing...")
                SyncWorker.run(
                    getApplication(),
                    syncCustomers = false,
                    syncEvents = false,
                    syncPayments = false,
                    syncTransactions = false,
                    syncSocios = false,
                ).result.get()
            }
    }
}
