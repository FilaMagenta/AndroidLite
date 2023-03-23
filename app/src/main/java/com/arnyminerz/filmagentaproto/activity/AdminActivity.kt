package com.arnyminerz.filmagentaproto.activity

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.arnyminerz.filmagentaproto.App
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.database.data.woo.Event
import com.arnyminerz.filmagentaproto.database.data.woo.Order
import com.arnyminerz.filmagentaproto.database.data.woo.ROLE_ADMINISTRATOR
import com.arnyminerz.filmagentaproto.database.local.AppDatabase
import com.arnyminerz.filmagentaproto.database.logic.getOrders
import com.arnyminerz.filmagentaproto.ui.components.NavigationBarItem
import com.arnyminerz.filmagentaproto.ui.components.NavigationBarItems
import com.arnyminerz.filmagentaproto.ui.components.Tooltip
import com.arnyminerz.filmagentaproto.ui.screens.admin.EventsAdminScreen
import com.arnyminerz.filmagentaproto.ui.theme.setContentThemed
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

private const val TAG = "AdminActivity"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
class AdminActivity : AppCompatActivity() {
    companion object {
        /**
         * Stores if the intent that launched the Activity comes from another activity, or from
         * a shortcut.
         */
        private const val EXTRA_PARENT_ACTIVITY = "parent"

        private const val SCAN_RESULT_OK = 0
        private const val SCAN_RESULT_LOADING = 1
        private const val SCAN_RESULT_FAIL = 2
        private const val SCAN_RESULT_INVALID = 3
    }

    object Contract : ActivityResultContract<Void?, Void?>() {
        override fun createIntent(context: Context, input: Void?): Intent =
            Intent(context, AdminActivity::class.java).apply {
                putExtra(EXTRA_PARENT_ACTIVITY, true)
            }

        override fun parseResult(resultCode: Int, intent: Intent?): Void? = null
    }

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        val contents: String? = result.contents
        viewModel.decodeQR(contents)
    }

    private val viewModel by viewModels<ViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val comesFromActivity = intent.getBooleanExtra(EXTRA_PARENT_ACTIVITY, false)

        setContentThemed {
            val scope = rememberCoroutineScope()

            // Handle back presses
            BackHandler { setResult(Activity.RESULT_CANCELED); finish() }

            // Store the current page selected
            val pagerState = rememberPagerState()

            val customer by viewModel.customer.observeAsState()
            LaunchedEffect(customer) {
                snapshotFlow { customer }
                    .filterNotNull()
                    .collect {
                        if (it.role != ROLE_ADMINISTRATOR) {
                            setResult(Activity.RESULT_CANCELED)
                            finish()
                        }
                    }
            }

            val scanResult by viewModel.scanResult.observeAsState()
            val scanCustomer by viewModel.scanCustomer.observeAsState()
            if (scanResult != null)
                ModalBottomSheet(
                    onDismissRequest = {
                        if (scanResult != SCAN_RESULT_LOADING) {
                            viewModel.scanResult.postValue(null)
                            viewModel.scanCustomer.postValue(null)
                        }
                    },
                ) {
                    when (scanResult) {
                        SCAN_RESULT_LOADING -> {
                            Text(
                                text = stringResource(R.string.admin_scan_loading),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                                    .padding(top = 16.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 22.sp,
                            )
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(top = 32.dp, bottom = 64.dp)
                            )
                        }
                        SCAN_RESULT_OK -> {
                            Icon(
                                Icons.Outlined.Verified,
                                null,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .size(96.dp)
                                    .padding(top = 32.dp),
                                tint = Color(0xff66ff66),
                            )
                            Text(
                                text = stringResource(R.string.admin_scan_correct),
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(top = 32.dp),
                                fontSize = 26.sp,
                            )
                            Text(
                                text = scanCustomer ?: "",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(bottom = 64.dp),
                                textAlign = TextAlign.Center,
                            )
                        }
                        SCAN_RESULT_INVALID -> {
                            Icon(
                                Icons.Outlined.Cancel,
                                null,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .size(96.dp)
                                    .padding(top = 32.dp),
                                tint = Color(0xffff3333),
                            )
                            Text(
                                text = stringResource(R.string.admin_scan_invalid),
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(bottom = 64.dp),
                                fontSize = 26.sp,
                            )
                        }
                    }
                }

            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(stringResource(R.string.admin_title)) },
                        navigationIcon = {
                            if (comesFromActivity)
                                IconButton(
                                    onClick = { setResult(Activity.RESULT_CANCELED); finish() },
                                ) {
                                    Icon(Icons.Rounded.ChevronLeft, stringResource(R.string.back))
                                }
                        },
                        actions = {
                            Box {
                                val showTooltip = remember { mutableStateOf(false) }

                                // Buttons and Surfaces don't support onLongClick out of the box,
                                // so use a simple Box with combinedClickable
                                Box(
                                    modifier = Modifier
                                        .combinedClickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = rememberRipple(),
                                            onClickLabel = "Button action description",
                                            role = Role.Button,
                                            onClick = { scanQRCode() },
                                            onLongClick = { showTooltip.value = true },
                                        ),
                                ) {
                                    Icon(Icons.Rounded.QrCodeScanner, "")
                                }

                                Tooltip(showTooltip) {
                                    // Tooltip content goes here.
                                    Text("Scan Code")
                                }
                            }
                        }
                    )
                },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItems(
                            selectedIndex = pagerState.currentPage,
                            onSelected = { scope.launch { pagerState.animateScrollToPage(it) } },
                            items = listOf(
                                NavigationBarItem(
                                    selectedIcon = Icons.Outlined.CalendarMonth,
                                    unselectedIcon = Icons.Filled.CalendarMonth,
                                    label = R.string.navigation_events,
                                )
                            ),
                        )
                    }
                },
            ) { paddingValues ->
                val events by viewModel.events.observeAsState()
                val customers by viewModel.customers.observeAsState(emptyList())

                HorizontalPager(pageCount = 1) { page ->
                    Column(
                        Modifier
                            .padding(paddingValues)
                            .padding(8.dp)
                    ) {
                        when (page) {
                            0 -> EventsAdminScreen(events, customers)
                        }
                    }
                }
            }
        }
    }

    private fun scanQRCode() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            // TODO: setPrompt()
        }
        viewModel.scanResult.postValue(SCAN_RESULT_LOADING)
        barcodeLauncher.launch(options)
    }

    class ViewModel(application: Application) : AndroidViewModel(application) {
        private val database = AppDatabase.getInstance(application)
        private val wooCommerceDao = database.wooCommerceDao()

        val customer = (application as App).customer

        val events = wooCommerceDao.getAllEventsLive().switchMap { events ->
            eventsOrdersLive(events)
        }

        val customers = wooCommerceDao.getAllCustomersLive()

        val scanResult = MutableLiveData<Int?>(null)
        val scanCustomer = MutableLiveData<String?>(null)

        private fun eventsOrdersLive(events: List<Event>): LiveData<List<Pair<Event, List<Order>>>> =
            MutableLiveData<List<Pair<Event, List<Order>>>>().apply {
                viewModelScope.launch(Dispatchers.IO) {
                    val result = mutableListOf<Pair<Event, List<Order>>>()
                    for (event in events) {
                        val orders = event.getOrders(getApplication())
                            .map { order ->
                                order.copy(items = order.items.filter { it.productId == event.id })
                            }
                        result.add(event to orders)
                    }
                    postValue(result)
                }
            }

        fun decodeQR(contents: String?) {
            if (contents == null)
                scanResult.postValue(SCAN_RESULT_FAIL)
            else {
                val customerName = Order.verifyQRCode(contents)
                if (customerName == null)
                    scanResult.postValue(SCAN_RESULT_INVALID)
                else {
                    scanResult.postValue(SCAN_RESULT_OK)
                    scanCustomer.postValue(customerName)
                }
            }
        }
    }
}