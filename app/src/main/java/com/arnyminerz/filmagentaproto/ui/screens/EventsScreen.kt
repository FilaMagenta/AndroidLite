package com.arnyminerz.filmagentaproto.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.activity.MainActivity
import com.arnyminerz.filmagentaproto.database.data.woo.Event
import com.arnyminerz.filmagentaproto.ui.components.EventItem
import com.arnyminerz.filmagentaproto.ui.components.LoadingBox
import com.arnyminerz.filmagentaproto.utils.toast
import java.util.Calendar
import kotlinx.coroutines.flow.filterNotNull

@Composable
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
fun EventsScreen(mainViewModel: MainActivity.MainViewModel) {
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
        val now = Calendar.getInstance().time.time

        return events
            // Filter past events
            ?.filter { event -> event.eventDate?.time?.let { it >= now } ?: true }
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
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(top = 8.dp),
        ) {
            stickyHeader {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(top = 20.dp)
                        .background(MaterialTheme.colorScheme.background),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EventAvailable,
                        contentDescription = stringResource(R.string.events_available_title),
                    )
                    Text(
                        text = stringResource(R.string.events_confirmed_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                    )
                }
            }
            items(
                processEventsList(confirmedEvents)
            ) { event ->
                EventItem(event, true) { _, _ -> }
            }
            stickyHeader {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(top = 20.dp)
                        .background(MaterialTheme.colorScheme.background),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EventNote,
                        contentDescription = stringResource(R.string.events_available_title),
                    )
                    Text(
                        text = stringResource(R.string.events_available_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                    )
                }
            }
            items(
                processEventsList(availableEvents)
            ) { event ->
                EventItem(event, false) { metadata, onComplete ->
                    mainViewModel
                        .signUpForEvent(customerState!!, event, metadata)
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
