package com.arnyminerz.filmagentaproto.activity

import android.Manifest
import android.accounts.Account
import android.accounts.AccountManager
import android.app.Application
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.work.WorkInfo
import com.arnyminerz.filmagentaproto.App
import com.arnyminerz.filmagentaproto.BuildConfig
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.account.Authenticator
import com.arnyminerz.filmagentaproto.account.Authenticator.Companion.USER_DATA_VERSION
import com.arnyminerz.filmagentaproto.database.data.Transaction
import com.arnyminerz.filmagentaproto.database.data.woo.Customer
import com.arnyminerz.filmagentaproto.database.data.woo.Event
import com.arnyminerz.filmagentaproto.database.data.woo.Order
import com.arnyminerz.filmagentaproto.database.data.woo.ROLE_ADMINISTRATOR
import com.arnyminerz.filmagentaproto.database.local.AppDatabase
import com.arnyminerz.filmagentaproto.database.logic.isConfirmed
import com.arnyminerz.filmagentaproto.database.remote.RemoteCommerce
import com.arnyminerz.filmagentaproto.database.remote.protos.Socio
import com.arnyminerz.filmagentaproto.exceptions.PaymentException
import com.arnyminerz.filmagentaproto.storage.SELECTED_ACCOUNT
import com.arnyminerz.filmagentaproto.storage.dataStore
import com.arnyminerz.filmagentaproto.ui.components.ErrorCard
import com.arnyminerz.filmagentaproto.ui.components.LoadingBox
import com.arnyminerz.filmagentaproto.ui.components.ModalDrawerSheetItem
import com.arnyminerz.filmagentaproto.ui.components.ModalNavigationDrawer
import com.arnyminerz.filmagentaproto.ui.components.NavigationBarItem
import com.arnyminerz.filmagentaproto.ui.components.NavigationBarItems
import com.arnyminerz.filmagentaproto.ui.components.ProfileImage
import com.arnyminerz.filmagentaproto.ui.dialogs.AccountMigrationDialog
import com.arnyminerz.filmagentaproto.ui.dialogs.AccountsDialog
import com.arnyminerz.filmagentaproto.ui.dialogs.PaymentBottomSheet
import com.arnyminerz.filmagentaproto.ui.dialogs.PaymentMadeBottomSheet
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
import com.arnyminerz.filmagentaproto.utils.launch
import com.arnyminerz.filmagentaproto.utils.launchTabsUrl
import com.arnyminerz.filmagentaproto.utils.launchUrl
import com.arnyminerz.filmagentaproto.utils.now
import com.arnyminerz.filmagentaproto.utils.toast
import com.arnyminerz.filmagentaproto.utils.trimmedAndCaps
import com.arnyminerz.filmagentaproto.utils.ui
import com.arnyminerz.filmagentaproto.worker.SyncWorker
import com.redsys.tpvvinapplibrary.ErrorResponse
import com.redsys.tpvvinapplibrary.IPaymentResult
import com.redsys.tpvvinapplibrary.ResultResponse
import com.redsys.tpvvinapplibrary.TPVV
import com.redsys.tpvvinapplibrary.TPVVConstants
import compose.icons.SimpleIcons
import compose.icons.simpleicons.Facebook
import compose.icons.simpleicons.Googleplay
import compose.icons.simpleicons.Instagram
import compose.icons.simpleicons.Telegram
import compose.icons.simpleicons.Tiktok
import compose.icons.simpleicons.Twitter
import io.sentry.Sentry
import java.util.Locale
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(
    ExperimentalTextApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
class MainActivity : AppCompatActivity() {
    companion object {
        val TOP_BAR_HEIGHT = (56 + 16).dp
    }

    private val viewModel by viewModels<MainViewModel>()

    private lateinit var am: AccountManager

    private lateinit var loginRequestLauncher: ActivityResultLauncher<LoginActivity.Contract.Data>

    private val notificationPermissionRequestLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (!granted) toast(R.string.error_toast_notifications)
    }

    private val eventViewRequestLauncher = registerForActivityResult(
        EventActivity.Contract
    ) { action ->
        if (action is EventActivity.ActionPerformed.DELETE) {
            viewModel.deleteEvent(action.eventId)
        }
    }

    private val adminScreenRequestLauncher = registerForActivityResult(AdminActivity.Contract) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        am = AccountManager.get(this)
        loginRequestLauncher = registerForActivityResult(LoginActivity.Contract()) { loggedIn ->
            val accounts = am.getAccountsByType(Authenticator.AuthTokenType)
            if (!loggedIn && accounts.isEmpty())
                finish()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAndRemoveTask()
            }
        })

        setContentThemed {
            val selectedAccountIndex by viewModel.selectedAccount.collectAsState(null)
            val accounts by viewModel.accounts.observeAsState(emptyList())

            val databaseData by viewModel.databaseData.observeAsState()

            var showingAccountsDialog by remember { mutableStateOf(false) }
            if (showingAccountsDialog)
                AccountsDialog(
                    accountsList = accounts,
                    selectedAccountIndex = selectedAccountIndex ?: -1,
                    onAccountSelected = { index, _ ->
                        Timber.i("Switching account to #$index")
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

            if (databaseData?.isEmpty() == true)
                return@setContentThemed InitialLoadScreen()

            var paymentMadeResult by remember { mutableStateOf<Pair<ResultResponse?, ErrorResponse?>?>(null) }
            paymentMadeResult?.let {
                PaymentMadeBottomSheet(it) { paymentMadeResult = null }
            }

            val processingPayment by viewModel.processingPayment.observeAsState(false)
            var showingPaymentBottomSheet by remember { mutableStateOf(false) }
            if (showingPaymentBottomSheet)
                PaymentBottomSheet(
                    isLoading = processingPayment,
                    onPaymentRequested = { amount, concept ->
                        if (!BuildConfig.DEBUG) {
                            // Use old method for production TODO: migrate to Redsys
                            viewModel.makePayment(amount, concept) { paymentUrl ->
                                showingPaymentBottomSheet = false
                                launchTabsUrl(paymentUrl)
                            }
                            return@PaymentBottomSheet
                        }

                        val orderCode = "APP" + SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
                            .format(now())

                        Timber.i("Making web payment for $amount € (${amount * 100})")
                        TPVV.doDirectPayment(
                            this,
                            orderCode,
                            amount * 100,
                            TPVVConstants.PAYMENT_TYPE_NORMAL,
                            null,
                            concept,
                            HashMap(),
                            object : IPaymentResult {
                                override fun paymentResultOK(response: ResultResponse) {
                                    paymentMadeResult = response to null
                                }

                                override fun paymentResultKO(response: ErrorResponse) {
                                    Timber.e("Could not make payment. Error (${response.code}): ${response.desc}")
                                    paymentMadeResult = null to response
                                    Sentry.captureException(PaymentException(response))
                                }
                            },
                        )
                    },
                    onDismissRequest = { showingPaymentBottomSheet = false },
                )

            var currentPage by remember { mutableStateOf(0) }

            selectedAccountIndex?.let { accountIndex ->
                Content(
                    currentPage, { currentPage = it }, accounts, accountIndex,
                    { showingAccountsDialog = true }, { showingPaymentBottomSheet = true },
                    databaseData,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()

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

    @Composable
    fun Content(
        currentPage: Int,
        onPageChanged: (Int) -> Unit,
        accounts: List<Account>,
        accountIndex: Int,
        onAccountsDialogRequested: () -> Unit,
        onPaymentBottomSheetRequested: () -> Unit,
        databaseData: List<Socio>?,
    ) {
        val scope = rememberCoroutineScope()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        ModalNavigationDrawer(
            items = listOf(
                ModalDrawerSheetItem(SimpleIcons.Telegram, R.string.telegram_channel) {

                },
                ModalDrawerSheetItem(Icons.Outlined.Language, R.string.website) {
                    launchUrl("https://filamagenta.com/")
                },
                ModalDrawerSheetItem.Divider,
                ModalDrawerSheetItem(SimpleIcons.Facebook, R.string.facebook) {
                    launchUrl("https://www.facebook.com/FilaMagenta/")
                },
                ModalDrawerSheetItem(SimpleIcons.Instagram, R.string.instagram) {
                    launchUrl("https://www.instagram.com/filamagenta/")
                },
                ModalDrawerSheetItem(SimpleIcons.Twitter, R.string.twitter) {
                    launchUrl("https://twitter.com/filamagenta")
                },
                ModalDrawerSheetItem(SimpleIcons.Tiktok, R.string.tiktok) {
                    launchUrl("https://www.tiktok.com/@filamagenta")
                },
                ModalDrawerSheetItem.Divider,
                ModalDrawerSheetItem(SimpleIcons.Googleplay, R.string.google_play) {
                    launchUrl("market://details?id=${BuildConfig.APPLICATION_ID}")
                },
            ),
            drawerState = drawerState,
        ) {
            Scaffold(
                topBar = {
                    AnimatedVisibility(
                        visible = currentPage == 0,
                        enter = slideInVertically(tween(durationMillis = 300)) { -it },
                        exit = slideOutVertically(tween(durationMillis = 300)) { -it },
                    ) {
                        CenterAlignedTopAppBar(
                            title = { Text(stringResource(R.string.app_name)) },
                            navigationIcon = {
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            drawerState.animateTo(
                                                DrawerValue.Open,
                                                tween(),
                                            )
                                        }
                                    },
                                ) {
                                    Icon(
                                        Icons.Rounded.Menu,
                                        stringResource(androidx.compose.ui.R.string.navigation_menu),
                                    )
                                }
                            },
                            actions = {
                                accounts
                                    .takeIf { it.isNotEmpty() }
                                    ?.getOrNull(accountIndex)
                                    ?.let {
                                        ProfileImage(
                                            name = it.name.uppercase(),
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .clickable { onAccountsDialogRequested() },
                                        )
                                    }
                                    ?: doAsync {
                                        if (accountIndex != 0)
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
                            onSelected = { onPageChanged(it) },
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
                    Row(verticalAlignment = Alignment.Bottom) {
                        val customer by viewModel.customer.collectAsState(initial = null)
                        if (customer?.role == ROLE_ADMINISTRATOR)
                            SmallFloatingActionButton(
                                onClick = { adminScreenRequestLauncher.launch(null) },
                                // containerColor = MaterialTheme.colorScheme.tertiary,
                                // contentColor = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.padding(end = 4.dp),
                            ) {
                                Icon(Icons.Outlined.AdminPanelSettings, "")
                            }

                        FloatingActionButton(onClick = onPaymentBottomSheetRequested) {
                            Icon(Icons.Rounded.Wallet, "")
                        }
                    }
                },
            ) { paddingValues ->
                var showingAccountMigrationDialog by remember { mutableStateOf(false) }
                if (showingAccountMigrationDialog)
                    AccountMigrationDialog {
                        val account = accounts[accountIndex]
                        Timber.w("Removing old account (${account.name}) and redirecting to login screen.")
                        am.removeAccountExplicitly(account)
                        LoginActivity::class.launch {
                            putExtra(LoginActivity.EXTRA_ACCOUNT_TYPE, account.type)
                            putExtra(LoginActivity.EXTRA_AUTH_TOKEN_TYPE, account.type)
                            putExtra(LoginActivity.EXTRA_DNI, account.name)
                        }
                        finish()
                    }

                val selectedAccount = accounts.getOrNull(accountIndex)
                val topPadding by animateDpAsState(
                    if (currentPage == 0)
                        TOP_BAR_HEIGHT
                    else
                        0.dp,
                    animationSpec = tween(durationMillis = 300),
                )

                LaunchedEffectFlow(accountIndex, { it }) { index ->
                    if (index < 0) return@LaunchedEffectFlow
                    val account = accounts.getOrNull(index) ?: return@LaunchedEffectFlow
                    val password: String? = am.getPassword(account)
                    val dni = password?.trimmedAndCaps
                    val socio = databaseData?.find { it.Dni?.trimmedAndCaps == dni }
                        ?: return@LaunchedEffectFlow
                    viewModel.getAssociatedAccounts(socio.idSocio)
                }

                LaunchedEffect(selectedAccount) {
                    snapshotFlow { selectedAccount }
                        .filterNotNull()
                        .collect { account ->
                            val version = am.getUserData(account, USER_DATA_VERSION)?.toIntOrNull()
                            if (version != Authenticator.VERSION)
                                showingAccountMigrationDialog = true
                        }
                }

                selectedAccount
                    ?.let { account ->
                        am.getUserData(account, Authenticator.USER_DATA_ID_SOCIO)?.toLongOrNull()
                    }
                    ?.let { idSocio ->
                        val transactionsLive = viewModel.transactionsDao.getByIdSocioLive(idSocio)
                        val socioData = databaseData?.find { it.idSocio == idSocio }
                        socioData to transactionsLive
                    }
                    ?.let { (socio, liveTransactions) ->
                        val pagerState = rememberPagerState()
                        val transactions by liveTransactions.observeAsState(emptyList())

                        LaunchedEffectFlow(pagerState, { it.currentPage }) {
                            onPageChanged(it)
                        }
                        LaunchedEffectFlow(
                            currentPage,
                            { it },
                        ) { pagerState.scrollToPage(it) }

                        HorizontalPager(
                            pageCount = 4,
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    top = topPadding,
                                    bottom = paddingValues.calculateBottomPadding()
                                ),
                        ) { page ->
                            when (page) {
                                0 -> MainPage(transactions, viewModel)
                                1 -> EventsScreen(viewModel) { event, customer ->
                                    eventViewRequestLauncher.launch(
                                        EventActivity.InputData(customer, event)
                                    )
                                }
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
                    // TODO: If list is empty, return to login screen
                    ?: LoadingBox()
            }
        }
    }

    class MainViewModel(application: Application) : AndroidViewModel(application) {
        private val database = AppDatabase.getInstance(application)
        val transactionsDao = database.transactionsDao()
        private val remoteDatabaseDao = database.remoteDatabaseDao()
        private val wooCommerceDao = database.wooCommerceDao()

        private val am = AccountManager.get(application)

        val selectedAccount = application
            .dataStore
            .data
            .map { preferences -> preferences[SELECTED_ACCOUNT] ?: 0 }

        val accounts = (application as App).accounts

        val databaseData = remoteDatabaseDao.getAllLive()

        val workerState = SyncWorker.getLiveStates(application)

        val isLoading = workerState.map { list ->
            list.any { it.state == WorkInfo.State.RUNNING }
        }

        val associatedAccountsTransactions = MutableLiveData<Map<Socio, List<Transaction>>>()

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

        fun getAssociatedAccounts(associatedWithId: Long) = async {
            val socios = remoteDatabaseDao.getAllAssociatedWith(associatedWithId)
            val personalDataList = socios.associateWith { socio ->
                transactionsDao.getByIdSocio(socio.idSocio)
            }
            Timber.i("Got ${socios.size} associated accounts for #$associatedWithId")
            associatedAccountsTransactions.postValue(personalDataList)
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

        @Deprecated("Use Redsys payment")
        fun makePayment(
            amount: Double,
            concept: String,
            @UiThread onComplete: (paymentUrl: String) -> Unit
        ) = async {
            val paymentUrl = try {
                processingPayment.postValue(true)
                Timber.d("Requesting a payment of $amount €. Getting customer...")
                val customer = customer.first()
                    ?: throw IllegalStateException("Could not get current customer.")
                Timber.d("Customer ID for payment: ${customer.id}")
                val payments = wooCommerceDao.getAllAvailablePayments()
                Timber.d("Making request...")
                val url = RemoteCommerce.transferAmount(amount, concept, payments, customer)
                Timber.i("Payment url: $url")
                url
            } catch (e: Exception) {
                Timber.e("Could not make payment: ${e.message}")
                null
            } finally {
                processingPayment.postValue(false)
            } ?: return@async
            ui { onComplete(paymentUrl) }
        }

        @Suppress("BlockingMethodInNonBlockingContext")
        fun signUpForEvent(
            customer: Customer,
            event: Event,
            metadata: List<Order.Metadata>,
            @UiThread onComplete: (paymentUrl: String) -> Unit
        ) = async {
            Timber.i("Signing up for event (price=${event.price}). Metadata: $metadata")
            val (paymentUrl, order) = RemoteCommerce.eventSignup(
                customer,
                "", // FIXME: Set notes
                event = event,
                metadata = metadata,
            )
            Timber.i("Adding order to database...")
            wooCommerceDao.insert(order)
            Timber.i("Event sign up is complete.")
            ui { onComplete(paymentUrl) }
        }

        fun deleteEvent(id: Long) = async {
            wooCommerceDao.deleteEvent(id)
        }
    }
}
