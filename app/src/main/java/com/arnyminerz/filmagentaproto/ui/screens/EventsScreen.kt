package com.arnyminerz.filmagentaproto.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.database.data.woo.Customer
import com.arnyminerz.filmagentaproto.database.data.woo.Event
import com.arnyminerz.filmagentaproto.ui.components.EventItem
import com.arnyminerz.filmagentaproto.ui.components.LoadingBox
import com.arnyminerz.filmagentaproto.ui.components.TabsColumn
import com.arnyminerz.filmagentaproto.utils.launchTabsUrl
import com.arnyminerz.filmagentaproto.utils.toast
import com.arnyminerz.filmagentaproto.viewmodel.MainViewModel
import kotlinx.coroutines.flow.filterNotNull
import timber.log.Timber

@Composable
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
fun EventsScreen(
    mainViewModel: MainViewModel,
    onEventSelected: (Event, Customer) -> Unit,
) {
    val context = LocalContext.current

    val customerState by mainViewModel.customer.collectAsState(initial = null)
    val events by mainViewModel.events.observeAsState()
    val orders by mainViewModel.orders.observeAsState()

    val confirmedEvents by mainViewModel.confirmedEvents.observeAsState()
    val availableEvents by mainViewModel.availableEvents.observeAsState()

    LaunchedEffect(events) {
        snapshotFlow { events }
            .filterNotNull()
            .collect { mainViewModel.updateConfirmedEvents(customerState) }
    }
    LaunchedEffect(orders) {
        snapshotFlow { orders }
            .filterNotNull()
            .collect { mainViewModel.updateConfirmedEvents(customerState) }
    }
    LaunchedEffect(customerState) {
        snapshotFlow { customerState }
            .filterNotNull()
            .collect { mainViewModel.updateConfirmedEvents(it) }
    }

    /**
     * Filters past events, and sorts them by proximity. If [events] is `null` an empty list is
     * returned.
     */
    fun processEventsList(events: List<Event>?): List<Event> {
        return events
            // Filter past events
            ?.filter { event -> !event.hasPassed }
            // Order by proximity or reservations limit
            // ?.sortedBy { event ->
            //     event.eventDate?.time ?: event.acceptsReservationsUntil?.time
            // }
            ?.sortedBy { it.index }
            ?: emptyList()
    }

    if (confirmedEvents == null)
        LoadingBox()
    else
        TabsColumn(
            modifier = Modifier.fillMaxSize(),
            tabs = setOf(
                stringResource(R.string.events_confirmed_title),
                stringResource(R.string.events_available_title),
            )
        ) { page ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                if (page == 0)
                    items(processEventsList(confirmedEvents)) { event ->
                        EventItem(
                            event = event,
                            isConfirmed = true,
                            isArchived = false,
                            onEventSelected = { onEventSelected(event, customerState!!) },
                        ) { _, _ -> }
                    }
                else if (page == 1)
                    items(
                        processEventsList(availableEvents)
                    ) { event ->
                        EventItem(
                            event,
                            isConfirmed = false,
                            isArchived = false,
                            onEventSelected = {},
                        ) { metadata, onComplete ->
                            mainViewModel
                                .signUpForEvent(
                                    customerState!!,
                                    event,
                                    metadata
                                ) { paymentUrl ->
                                    if (event.price > 0.0) {
                                        Timber.i("Event is not free, redirecting to the payment gateway...")
                                        context.launchTabsUrl(paymentUrl)
                                    }
                                }
                                .invokeOnCompletion { error ->
                                    if (error != null)
                                        context.toast(error.message ?: error.localizedMessage)
                                    else
                                        onComplete()
                                }
                        }
                    }
            }
        }
}
