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
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.EuroSymbol
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material3.AssistChip
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.database.data.admin.CodeScanned
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
import com.arnyminerz.filmagentaproto.ui.dialogs.admin.MarkPaidDialog
import com.arnyminerz.filmagentaproto.ui.dialogs.admin.ScanResultBottomSheet
import com.arnyminerz.filmagentaproto.ui.theme.SuccessColor
import com.arnyminerz.filmagentaproto.ui.theme.setContentThemed
import com.arnyminerz.filmagentaproto.utils.async
import com.arnyminerz.filmagentaproto.utils.await
import com.arnyminerz.filmagentaproto.utils.getParcelableExtraCompat
import com.arnyminerz.filmagentaproto.utils.launch
import com.arnyminerz.filmagentaproto.utils.toastAsync
import com.arnyminerz.filmagentaproto.worker.TicketWorker
import java.util.Locale
import timber.log.Timber

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
class AdminEventActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_EVENT = "event"

        const val RESULT_ERROR = "error"
        const val RESULT_ERROR_MESSAGE = "error_message"

        const val SCAN_RESULT_OK = 0
        const val SCAN_RESULT_LOADING = 1
        const val SCAN_RESULT_FAIL = 2
        const val SCAN_RESULT_INVALID = 3
        const val SCAN_RESULT_REPEATED = 4
        const val SCAN_RESULT_OLD = 5

        private const val SORT_BY_NAME = 0
        private const val SORT_BY_ORDER = 1
        private const val SORT_BY_SCANNED = 2
        private const val SORT_BY_PAID = 3

        const val ERROR_MISSING_EVENT = "missing-event"
        const val ERROR_EVENT_NOT_FOUND = "event-not-found"
        const val ERROR_BACK_PRESSED = "back-pressed"

        const val TEST_TAG_TITLE = "title"
        const val TEST_TAG_BACK = "back"

        private fun errorIntent(throwable: Throwable? = null, message: String? = null) =
            Intent().apply {
                putExtra(RESULT_ERROR, throwable)
                putExtra(RESULT_ERROR_MESSAGE, message)
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

    private val barcodeLauncher = registerForActivityResult(ScannerActivity.Contract) { result ->
        if (result == null) {
            Timber.w("Got a null result from the barcode launcher.")
            viewModel.decodeQR(null)
        } else
            result.forEach { viewModel.decodeQR(it) }
    }

    private val savePdfLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { if (it != null) viewModel.generatePdf(it) }

    private val viewModel by viewModels<ViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val eventId = intent.getLongExtra(EXTRA_EVENT, -1)
        if (eventId < 0) {
            Timber.w("Launched AdminEventActivity without passing an event.")
            setResult(Activity.RESULT_CANCELED, errorIntent(message = ERROR_MISSING_EVENT))
            finish()
            return
        }

        viewModel.load(eventId).invokeOnCompletion {
            if (viewModel.event.value == null) {
                Timber.w("Launched AdminEventActivity with an event that doesn't exist ($eventId).")
                setResult(Activity.RESULT_CANCELED, errorIntent(it, ERROR_EVENT_NOT_FOUND))
                finish()
            }
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                setResult(Activity.RESULT_CANCELED, errorIntent(message = ERROR_BACK_PRESSED))
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
            var confirmingMarkPaid by remember { mutableStateOf<String?>(null) }
            confirmingMarkPaid?.let { customerName ->
                MarkPaidDialog(
                    customerName = customerName,
                    eventName = event.name,
                    onConfirmRequest = { /*TODO*/ },
                    onDismissRequest = { confirmingMarkPaid = null },
                )
            }

            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(event.title, modifier = Modifier.testTag(TEST_TAG_TITLE)) },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    setResult(
                                        Activity.RESULT_CANCELED,
                                        errorIntent(message = ERROR_BACK_PRESSED),
                                    )
                                    finish()
                                },
                                modifier = Modifier.testTag(TEST_TAG_BACK),
                            ) { Icon(Icons.Rounded.ChevronLeft, stringResource(R.string.back)) }
                        },
                    )
                }
            ) { paddingValues ->
                val orders by viewModel.orders.observeAsState(initial = emptyList())
                val ordersCustomerMap by viewModel.ordersCustomer.observeAsState(initial = emptyMap())
                var sortBy by remember { mutableStateOf(SORT_BY_NAME) }
                val codes by viewModel.adminDao.getAllScannedCodesLive().observeAsState()

                LazyColumn(
                    Modifier
                        .padding(paddingValues)
                        .padding(horizontal = 8.dp),
                ) {
                    // Event Information
                    item { GeneralInformation(event, orders) }

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
                                FilterChip(
                                    selected = sortBy == SORT_BY_PAID,
                                    onClick = { sortBy = SORT_BY_PAID },
                                    label = { Text(stringResource(R.string.admin_events_sort_paid)) },
                                    leadingIcon = { Icon(Icons.Rounded.EuroSymbol, null) },
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                )
                            }
                        }
                    }

                    // All the people signed up
                    items(
                        ordersCustomerMap.toList()
                            .let { list ->
                                when (sortBy) {
                                    SORT_BY_NAME -> list.sortedBy { (_, customer) ->
                                        customer.firstName
                                    }
                                    SORT_BY_ORDER -> list.sortedBy { (order, _) ->
                                        order.id
                                    }
                                    SORT_BY_SCANNED -> list.sortedByDescending { (order, _) ->
                                        codes?.find { it.hash == order.hash } != null
                                    }
                                    SORT_BY_PAID -> list.sortedBy { (order, _) ->
                                        order.payment?.any
                                    }
                                    else -> list.sortedBy { (order, _) -> "#${order.id}" }
                                }
                            }
                    ) { (order, customer) ->
                        val scannedCode by viewModel.adminDao.getFromHashLive(order.hash)
                            .observeAsState()

                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (scannedCode != null)
                                    Icon(
                                        Icons.Rounded.RadioButtonChecked,
                                        stringResource(R.string.admin_events_scanned),
                                        tint = SuccessColor,
                                        modifier = Modifier.padding(start = 4.dp),
                                    )
                                Text(
                                    text = customer.fullName,
                                    modifier = Modifier
                                        .padding(top = 12.dp)
                                        .padding(start = 8.dp)
                                        .weight(1f),
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                            ) {
                                if (order.payment?.any == true) {
                                    AssistChip(
                                        onClick = { /*TODO*/ },
                                        label = { Text("Paid") },
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp),
                                    )
                                } else {
                                    AssistChip(
                                        onClick = { confirmingMarkPaid = customer.fullName },
                                        label = { Text("Not Paid") },
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp),
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
        } ?: LoadingBox()
    }

    @Composable
    fun GeneralInformation(event: Event, orders: List<Order>) {
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
        InformationCard(
            icon = Icons.Outlined.People,
            title = stringResource(R.string.admin_event_general_people),
            message = stringResource(
                R.string.admin_event_general_people_total,
                orders.sumOf { order ->
                    order.items
                        .filter { it.productId == event.id }
                        .sumOf { it.quantity }
                },
            ) + '\n' + orders
                // Flatten all the orders for the current event
                .flatMap { order -> order.items.filter { it.productId == event.id } }
                // Group by variation
                .groupBy { it.variationId }
                // Take only non-empty lists
                .filter { (_, orders) -> orders.isNotEmpty() }
                // Convert to list of pairs
                .toList()
                .joinToString { (variationId, orders) ->
                    val matchingOptions = event.attributes
                        .map { attr -> attr.options.find { it.variationId == variationId } }
                    val amount = orders.sumOf { it.quantity }
                    // Since orders have been grouped by variationId, all of them have the same name
                    val name = matchingOptions.firstOrNull()?.displayValue
                    "$name: $amount"
                },
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
        viewModel.scanResult.postValue(SCAN_RESULT_LOADING)
        viewModel.scanCustomer.postValue(null)
        barcodeLauncher.launch()
    }

    class ViewModel(application: Application) : AndroidViewModel(application) {
        private val database = AppDatabase.getInstance(application)

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        val wooCommerceDao = database.wooCommerceDao()
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
            wooCommerceDao.getEvent(eventId)?.let { loadedEvent ->
                event.postValue(loadedEvent)

                Timber.d("Loading event orders...")
                val ordersList = loadedEvent.getOrders(getApplication())
                orders.postValue(ordersList)

                Timber.d("Loading order customers...")
                val ordersCustomerList =
                    ordersList.associateWith { wooCommerceDao.getCustomer(it.customerId)!! }
                ordersCustomer.postValue(ordersCustomerList)
            }
        }

        /**
         * Takes the contents read from a QR, processes them, and outputs a result into [scanResult].
         * Updates [scanCustomer] with the resulting customer if the code is valid.
         */
        fun decodeQR(contents: String?) = async {
            if (contents == null)
                scanResult.postValue(SCAN_RESULT_FAIL)
            else
                try {
                    val verification = Order.verifyQRCode(contents)
                    if (verification == null)
                        scanResult.postValue(SCAN_RESULT_INVALID)
                    else {
                        if (adminDao.getFromHash(verification.hash) != null) {
                            // Notify about a repeated code
                            scanResult.postValue(SCAN_RESULT_REPEATED)
                        } else {
                            // Insert the scanned code
                            val codeScanned = CodeScanned(0, verification.hash)
                            adminDao.insert(codeScanned)

                            scanResult.postValue(SCAN_RESULT_OK)
                        }
                        scanCustomer.postValue(verification.customerName)
                    }
                } catch (e: SecurityException) {
                    scanResult.postValue(SCAN_RESULT_OLD)
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
                val tickets = with(getApplication<Application>()) {
                    TicketWorker.TicketData.fromOrders(
                        wooCommerceDao,
                        event,
                        orders,
                    ) { current, value -> ticketsProgress.postValue(current to value) }
                }

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