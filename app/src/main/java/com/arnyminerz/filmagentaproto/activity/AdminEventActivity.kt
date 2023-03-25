package com.arnyminerz.filmagentaproto.activity

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.database.data.admin.ScannedCode
import com.arnyminerz.filmagentaproto.database.data.woo.Customer
import com.arnyminerz.filmagentaproto.database.data.woo.Event
import com.arnyminerz.filmagentaproto.database.data.woo.Order
import com.arnyminerz.filmagentaproto.database.local.AppDatabase
import com.arnyminerz.filmagentaproto.database.logic.getOrders
import com.arnyminerz.filmagentaproto.database.logic.verifyQRCode
import com.arnyminerz.filmagentaproto.ui.components.ButtonWithIcon
import com.arnyminerz.filmagentaproto.ui.components.InformationCard
import com.arnyminerz.filmagentaproto.ui.components.LoadingBox
import com.arnyminerz.filmagentaproto.ui.dialogs.admin.GeneratingTicketsDialog
import com.arnyminerz.filmagentaproto.ui.dialogs.admin.ScanResultBottomSheet
import com.arnyminerz.filmagentaproto.ui.theme.setContentThemed
import com.arnyminerz.filmagentaproto.utils.async
import com.arnyminerz.filmagentaproto.utils.await
import com.arnyminerz.filmagentaproto.utils.getParcelableExtraCompat
import com.arnyminerz.filmagentaproto.utils.toastAsync
import com.arnyminerz.filmagentaproto.worker.TicketWorker
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.util.Locale
import timber.log.Timber

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
class AdminEventActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_EVENT = "event"

        const val RESULT_ERROR = "error"

        const val SCAN_RESULT_OK = 0
        const val SCAN_RESULT_LOADING = 1
        const val SCAN_RESULT_FAIL = 2
        const val SCAN_RESULT_INVALID = 3
        const val SCAN_RESULT_REPEATED = 4

        private const val SORT_BY_NAME = 0
        private const val SORT_BY_ORDER = 1
        private const val SORT_BY_SCANNED = 2

        private fun errorIntent(throwable: Throwable) = Intent().apply {
            putExtra(RESULT_ERROR, throwable)
        }
    }

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    class Contract : ActivityResultContract<Event, Throwable?>() {
        override fun createIntent(context: Context, input: Event): Intent =
            Intent(context, AdminEventActivity::class.java).apply {
                putExtra(EXTRA_EVENT, input.id)
            }

        override fun parseResult(resultCode: Int, intent: Intent?): Throwable? =
            intent?.getParcelableExtraCompat(RESULT_ERROR, Throwable::class)
    }

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        val contents: String? = result.contents
        viewModel.decodeQR(contents)
    }

    private val savePdfLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) viewModel.generatePdf(uri)
    }

    private val viewModel by viewModels<ViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val eventId = intent.getLongExtra(EXTRA_EVENT, -1)
        if (eventId < 0) {
            Timber.w("Launched AdminEventActivity without passing an event.")
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        viewModel.load(eventId).invokeOnCompletion { error ->
            if (error != null) {
                setResult(Activity.RESULT_CANCELED, errorIntent(error))
                finish()
            }
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        })

        setContentThemed {
            val scanResult by viewModel.scanResult.observeAsState()
            val scanCustomer by viewModel.scanCustomer.observeAsState()
            scanResult?.let {
                ScanResultBottomSheet(scanResult = it, scanCustomer) {
                    if (scanResult != SCAN_RESULT_LOADING) {
                        viewModel.scanResult.postValue(null)
                        viewModel.scanCustomer.postValue(null)
                    }
                }
            }

            val ticketsProgress by viewModel.ticketsProgress.observeAsState()
            val pdfGenerationState by viewModel.pdfGenerationProgress.observeAsState()
            ticketsProgress?.let {
                GeneratingTicketsDialog(it, pdfGenerationState)
            }

            Content()
        }
    }

    @Composable
    fun Content() {
        val eventState by viewModel.event.observeAsState()

        eventState?.let { event ->
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(event.title) },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    setResult(Activity.RESULT_CANCELED)
                                    finish()
                                },
                            ) { Icon(Icons.Rounded.ChevronLeft, stringResource(R.string.back)) }
                        },
                    )
                }
            ) { paddingValues ->
                val orders by viewModel.ordersCustomer.observeAsState(initial = emptyMap())
                var sortBy by remember { mutableStateOf(SORT_BY_NAME) }
                val codes by viewModel.adminDao.getAllScannedCodesLive().observeAsState()

                LazyColumn(
                    Modifier
                        .padding(paddingValues)
                        .padding(horizontal = 8.dp),
                ) {
                    // Event Information
                    item { GeneralInformation(event) }

                    // Action Buttons
                    item { Actions(event) }

                    // Header for people
                    stickyHeader {
                        Column {
                            Text(
                                text = stringResource(R.string.admin_event_people),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 26.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background),
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BottomSheetDefaults.ContainerColor)
                                    .padding(horizontal = 8.dp)
                                    .horizontalScroll(rememberScrollState()),
                            ) {
                                FilterChip(
                                    selected = sortBy == SORT_BY_NAME,
                                    onClick = { sortBy = SORT_BY_NAME },
                                    label = { Text(stringResource(R.string.admin_events_sort_name)) },
                                    leadingIcon = { Icon(Icons.Rounded.FontDownload, null) },
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                )
                                FilterChip(
                                    selected = sortBy == SORT_BY_ORDER,
                                    onClick = { sortBy = SORT_BY_ORDER },
                                    label = { Text(stringResource(R.string.admin_events_sort_order)) },
                                    leadingIcon = { Icon(Icons.Rounded.Numbers, null) },
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                )
                                FilterChip(
                                    selected = sortBy == SORT_BY_SCANNED,
                                    onClick = { sortBy = SORT_BY_SCANNED },
                                    label = { Text(stringResource(R.string.admin_events_sort_scanned)) },
                                    leadingIcon = { Icon(Icons.Rounded.QrCode2, null) },
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                )
                            }
                        }
                    }

                    // All the people signed up
                    items(
                        orders.toList()
                            .sortedBy { (order, customer) ->
                                when (sortBy) {
                                    SORT_BY_NAME -> customer.firstName
                                    SORT_BY_ORDER -> "${order.id}"
                                    SORT_BY_SCANNED -> (codes?.find { it.hashCode == order.hashCode().toLong() } != null).toString()
                                    else -> "#${order.id}"
                                }
                            }
                    ) { (order, customer) ->
                        val hashCode = order.hashCode().toLong()
                        val scannedCode by viewModel.adminDao.getFromHashCodeLive(hashCode).observeAsState()

                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = customer.let { it.firstName + " " + it.lastName },
                                modifier = Modifier
                                    .padding(12.dp)
                            )
                            if (scannedCode != null)
                                Text("Scanned!")
                        }
                    }
                }
            }
        } ?: LoadingBox()
    }

    @Composable
    fun GeneralInformation(event: Event) {
        Text(
            text = stringResource(R.string.admin_event_general),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            fontSize = 26.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        InformationCard(
            icon = Icons.Outlined.CalendarMonth,
            title = stringResource(R.string.admin_event_general_date),
            message = event.eventDate?.let { dateFormatter.format(it) }
                ?: stringResource(R.string.none),
            modifier = Modifier.padding(vertical = 4.dp),
        )
        InformationCard(
            icon = Icons.Outlined.EventBusy,
            title = stringResource(R.string.admin_event_general_reservations),
            message = event.acceptsReservationsUntil?.let { dateFormatter.format(it) }
                ?: stringResource(R.string.none),
            modifier = Modifier.padding(vertical = 4.dp),
        )
    }

    @Composable
    fun Actions(event: Event) {
        Text(
            text = stringResource(R.string.admin_event_actions),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            fontSize = 26.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        ButtonWithIcon(
            icon = Icons.Outlined.QrCodeScanner,
            text = stringResource(R.string.admin_event_actions_scan),
        ) { scanQRCode() }
        ButtonWithIcon(
            icon = Icons.Outlined.PictureAsPdf,
            text = stringResource(R.string.admin_events_export_tickets),
        ) { savePdfLauncher.launch(event.title + ".pdf") }
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
        val adminDao = database.adminDao()

        val event = MutableLiveData<Event>()

        val scanResult = MutableLiveData<Int?>(null)
        val scanCustomer = MutableLiveData<String?>(null)

        val ticketsProgress = MutableLiveData<Pair<Int, Int>?>(null)
        val pdfGenerationProgress = TicketWorker.workerState(application)

        val orders = MutableLiveData<List<Order>>()

        val ordersCustomer = MutableLiveData<Map<Order, Customer>>()

        /**
         * Loads the event to be used into [event] from its id.
         */
        fun load(eventId: Long) = async {
            Timber.d("Loading event #$eventId")
            val loadedEvent = wooCommerceDao.getEvent(eventId)!!
            event.postValue(loadedEvent)

            Timber.d("Loading event orders...")
            val ordersList = loadedEvent.getOrders(getApplication())
            orders.postValue(ordersList)

            Timber.d("Loading order customers...")
            val ordersCustomerList = ordersList.associateWith { wooCommerceDao.getCustomer(it.customerId)!! }
            ordersCustomer.postValue(ordersCustomerList)
        }

        /**
         * Takes the contents read from a QR, processes them, and outputs a result into [scanResult].
         * Updates [scanCustomer] with the resulting customer if the code is valid.
         */
        fun decodeQR(contents: String?) = async {
            if (contents == null)
                scanResult.postValue(SCAN_RESULT_FAIL)
            else {
                val verification = Order.verifyQRCode(contents)
                if (verification == null)
                    scanResult.postValue(SCAN_RESULT_INVALID)
                else {
                    if (adminDao.getFromHashCode(verification.hashCode) != null) {
                        // Notify about a repeated code
                        scanResult.postValue(SCAN_RESULT_REPEATED)
                    } else {
                        // Insert the scanned code
                        val scannedCode = ScannedCode(verification.hashCode)
                        adminDao.insert(scannedCode)

                        scanResult.postValue(SCAN_RESULT_OK)
                    }
                    scanCustomer.postValue(verification.customerName)
                }
            }
        }

        /**
         * Generates the PDF for tickets from the orders in [orders].
         */
        @Suppress("BlockingMethodInNonBlockingContext")
        fun generatePdf(target: Uri) = async {
            try {
                ticketsProgress.postValue(0 to 0)

                val event = event.await()
                val orders = orders.await()
                val tickets = TicketWorker.TicketData.fromOrders(
                    wooCommerceDao,
                    event,
                    orders,
                ) { current, value -> ticketsProgress.postValue(current to value) }

                // Start generating
                val workerState = TicketWorker.generate(
                    getApplication(), tickets, target,
                )

                // Wait until worker ends
                workerState.result.get()

                getApplication<Application>().toastAsync(R.string.admin_tickets_ready)
            } finally {
                Timber.d("Finished generating tickets PDF.")
                ticketsProgress.postValue(null)
            }
        }
    }
}