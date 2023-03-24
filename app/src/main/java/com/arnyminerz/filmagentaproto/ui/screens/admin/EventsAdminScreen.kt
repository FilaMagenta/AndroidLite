package com.arnyminerz.filmagentaproto.ui.screens.admin

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.database.data.woo.Customer
import com.arnyminerz.filmagentaproto.database.data.woo.Event
import com.arnyminerz.filmagentaproto.database.data.woo.Order
import com.arnyminerz.filmagentaproto.ui.components.LoadingBox
import com.arnyminerz.filmagentaproto.ui.components.admin.AdminEventItem
import com.arnyminerz.filmagentaproto.ui.dialogs.admin.PeopleListBottomSheet
import com.arnyminerz.filmagentaproto.utils.by
import com.arnyminerz.filmagentaproto.utils.choose
import com.arnyminerz.filmagentaproto.utils.now

@Composable
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
fun ColumnScope.EventsAdminScreen(
    events: List<Pair<Event, List<Order>>>?,
    customers: List<Customer>,
    onPdfExport: (Event) -> Unit,
) {
    Text(
        text = stringResource(R.string.admin_events_title),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        modifier = Modifier.fillMaxWidth(),
    )
    Text(
        text = stringResource(R.string.admin_events_message),
        style = MaterialTheme.typography.bodyMedium,
        fontSize = 14.sp,
        modifier = Modifier.fillMaxWidth(),
    )

    var showPastEvents by remember { mutableStateOf(false) }

    LazyColumn(
        Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(top = 8.dp),
    ) {
        item {
            if (events == null)
                LoadingBox()
        }
        // Filter chips
        item {
            Row(Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = showPastEvents,
                    onClick = { showPastEvents = !showPastEvents },
                    label = { Text(stringResource(R.string.admin_events_filter_past)) },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Visibility choose Icons.Rounded.VisibilityOff by showPastEvents,
                            stringResource(
                                R.string.enabled choose R.string.disabled by showPastEvents,
                            ),
                        )
                    }
                )
            }
        }
        items(
            events
                ?.filter { (event, _) ->
                    if (!showPastEvents)
                        event.eventDate?.let { eventDate ->
                            eventDate >= now()
                        } ?: true
                    else
                        true
                }
                ?.sortedByDescending { (event, _) -> event.eventDate }
                ?: emptyList()
        ) { (event, orders) ->
            var showSheet by remember { mutableStateOf(false) }
            if (showSheet)
                PeopleListBottomSheet(peopleList = orders, customers = customers) {
                    showSheet = false
                }

            AdminEventItem(
                event,
                orders,
                onPeopleListRequested = { showSheet = true },
                onTicketsListRequested = { onPdfExport(event) },
            )
        }
    }
}