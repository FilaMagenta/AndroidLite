package com.arnyminerz.filmagentaproto.activity

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.arnyminerz.filamagenta.core.database.data.woo.EventProto
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.database.data.woo.Customer
import com.arnyminerz.filmagentaproto.database.data.woo.Event
import com.arnyminerz.filmagentaproto.database.data.woo.Order
import com.arnyminerz.filmagentaproto.database.local.AppDatabase
import com.arnyminerz.filmagentaproto.database.logic.getQRCode
import com.arnyminerz.filmagentaproto.database.remote.RemoteCommerce
import com.arnyminerz.filmagentaproto.ui.components.HtmlText
import com.arnyminerz.filmagentaproto.ui.components.LoadingBox
import com.arnyminerz.filmagentaproto.ui.theme.setContentThemed
import com.arnyminerz.filmagentaproto.utils.async
import com.arnyminerz.filmagentaproto.utils.getParcelableExtraCompat
import com.arnyminerz.filmagentaproto.utils.launchCalendarInsert
import java.util.Locale
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
class EventActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_EVENT_ID = "event"
        private const val EXTRA_CUSTOMER_ID = "customer"

        private const val RESULT_ACTION = "action"

        private fun errorIntent(error: Throwable) = resultIntent(ActionPerformed.ERROR(error))

        private fun resultIntent(action: ActionPerformed) = Intent().apply {
            putExtra(RESULT_ACTION, action)
        }
    }

    private val viewModel by viewModels<EventViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1)
        val customerId = intent.getLongExtra(EXTRA_CUSTOMER_ID, -1)
        if (eventId < 0 || customerId < 0) {
            Timber.e("Tried to launch activity without any Event or Customer.")
            setResult(Activity.RESULT_CANCELED, errorIntent(IllegalArgumentException("Missing extras.")))
            finish()
            return
        }

        viewModel.load(eventId, customerId).invokeOnCompletion { error ->
            if (error != null) {
                Timber.e("Could not load event.", error)
                setResult(Activity.RESULT_CANCELED, errorIntent(error))
                finish()
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        })

        setContentThemed {
            val eventState by viewModel.event.observeAsState()
            val customerState by viewModel.customer.observeAsState()
            val orderState by viewModel.order.observeAsState()
            val loaded by viewModel.loaded.observeAsState(false)

            if (loaded) {
                val event = eventState!!
                val customer = customerState!!
                val order = orderState!!

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
                                ) {
                                    Icon(
                                        Icons.Rounded.ChevronLeft,
                                        stringResource(R.string.back)
                                    )
                                }
                            },
                        )
                    },
                ) { paddingValues ->
                    Column(
                        Modifier
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        Contents(event, customer, order)
                    }
                }
            } else
                LoadingBox()
        }
    }

    @Composable
    fun DeleteDialog(event: EventProto, customer: Customer, onDismissRequest: () -> Unit) {
        var isDeleting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isDeleting) onDismissRequest() },
            title = { Text(stringResource(R.string.events_cancel_reservation_title)) },
            icon = {
                Icon(
                    Icons.Rounded.DeleteForever,
                    stringResource(R.string.events_cancel_reservation_title),
                )
            },
            text = {
                Text(
                    stringResource(
                        R.string.events_cancel_reservation_message,
                        event.title,
                    )
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !isDeleting,
                    onClick = {
                        isDeleting = true
                        viewModel.cancelEventReservation(customer, event)
                            .invokeOnCompletion {
                                isDeleting = false
                                setResult(
                                    Activity.RESULT_OK,
                                    Intent().apply {
                                        putExtra(RESULT_ACTION, ActionPerformed.DELETE(event.id))
                                    },
                                )
                            }
                    },
                ) { Text(stringResource(R.string.yes)) }
            },
            dismissButton = {
                TextButton(
                    enabled = !isDeleting,
                    onClick = { onDismissRequest() },
                ) { Text(stringResource(R.string.cancel)) }
            },
        )
    }

    @Composable
    fun Contents(event: EventProto, customer: Customer, order: Order) {
        var showingCancelDialog by remember { mutableStateOf(false) }
        if (showingCancelDialog)
            DeleteDialog(event, customer) { showingCancelDialog = false }

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.event_view_info),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            )

            event.cutDescription
                .takeIf { it.isNotBlank() }
                ?.let {
                    HtmlText(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                    )
                }
            event.eventDate?.let { date ->
                val dateStr = remember {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(date)
                }

                Text(
                    text = stringResource(R.string.events_event_date, dateStr),
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                )
            }

            OutlinedButton(
                onClick = { showingCancelDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
                    .padding(top = 8.dp),
            ) {
                Icon(Icons.Outlined.DeleteForever, stringResource(R.string.cancel))
                Text(
                    text = stringResource(R.string.cancel),
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 8.dp),
                )
            }
            event.eventDate?.let { eventDate ->
                OutlinedButton(
                    onClick = {
                        launchCalendarInsert(
                            begin = eventDate,
                            title = event.title,
                            description = event.description,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .padding(bottom = 8.dp),
                ) {
                    Icon(Icons.Outlined.EditCalendar, stringResource(R.string.add_to_calendar))
                    Text(
                        text = stringResource(R.string.add_to_calendar),
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 8.dp),
                    )
                }
            }
        }

        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(top = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.event_view_qr),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            )

            val qr = remember { order.getQRCode(customer) }
            Image(
                bitmap = qr.asImageBitmap(),
                contentDescription = "",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(8.dp)
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit,
            )
        }
    }

    class EventViewModel(application: Application) : AndroidViewModel(application) {
        val event: MutableLiveData<EventProto> = MutableLiveData()
        val customer: MutableLiveData<Customer> = MutableLiveData()
        val order: MutableLiveData<Order> = MutableLiveData()

        val loaded = MutableLiveData<Boolean>()

        private val database = AppDatabase.getInstance(application)
        private val dao = database.wooCommerceDao()

        fun load(eventId: Long, customerId: Long) = async {
            Timber.d("Loading event #$eventId for customer #$customerId")

            val event = dao.getEvent(eventId)
                .also {
                    if (it == null)
                        Timber.e("Could not find a customer with id $customerId")
                    else
                        Timber.d("Event loaded!")
                }
            this@EventViewModel.event.postValue(event)

            val customer = dao.getCustomer(customerId)
                .also {
                    if (it == null)
                        Timber.e("Could not find a customer with id $customerId")
                    else
                        Timber.d("Customer loaded!")
                }
            this@EventViewModel.customer.postValue(customer)

            var order: Order? = null
            if (event != null && customer != null) {
                Timber.d("Getting all orders...")
                val orders = dao.getAllOrders()
                Timber.d("Searching for order...")
                order = orders
                    .filter { it.customerId == customer.id }
                    .firstOrNull { item -> item.items.find { it.productId == event.id } != null }
                    .also {
                        if (it == null)
                            Timber.e("Could not find an order from event and customer!")
                        else
                            Timber.d("Order loaded!")
                    }
            } else
                Timber.w("Won't load order.")
            this@EventViewModel.order.postValue(order)

            if (event == null || customer == null || order == null)
                throw IllegalStateException("Some data could not be loaded.")

            loaded.postValue(true)
        }

        @Suppress("BlockingMethodInNonBlockingContext")
        fun cancelEventReservation(
            customer: Customer,
            event: EventProto,
        ) = async {
            val order = order.value
            if (order == null) {
                Timber.w("Could not find a matching order for event #$event and customer #$customer")
                return@async
            }
            RemoteCommerce.eventCancel(order.id)
            Timber.i("Event cancelled. Deleting from db...")
            dao.delete(event as Event)
        }
    }

    @Parcelize
    sealed class ActionPerformed(val name: String) : Parcelable {
        data class DELETE(val eventId: Long) : ActionPerformed("DELETE")

        data class ERROR(val exception: Throwable): ActionPerformed("ERROR")
    }

    data class InputData(
        val customer: Customer,
        val event: EventProto,
    )

    object Contract : ActivityResultContract<InputData, ActionPerformed?>() {
        override fun createIntent(context: Context, input: InputData): Intent =
            Intent(context, EventActivity::class.java).apply {
                putExtra(EXTRA_EVENT_ID, input.event.id)
                putExtra(EXTRA_CUSTOMER_ID, input.customer.id)
            }

        override fun parseResult(resultCode: Int, intent: Intent?): ActionPerformed? =
            intent?.getParcelableExtraCompat(RESULT_ACTION, ActionPerformed::class)
    }
}
