package com.arnyminerz.filmagentaproto.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.activity.MainActivity
import com.arnyminerz.filmagentaproto.ui.components.EventItem
import com.arnyminerz.filmagentaproto.ui.components.LoadingBox
import java.util.Calendar
import kotlinx.coroutines.flow.filterNotNull

@Composable
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
fun EventsScreen(mainViewModel: MainActivity.MainViewModel) {
    val customerState by mainViewModel.customer.collectAsState(initial = null)
    val events by mainViewModel.events.observeAsState()

    val confirmedEvents by mainViewModel.confirmedEvents.observeAsState()
    val availableEvents by mainViewModel.availableEvents.observeAsState()

    LaunchedEffect(events) {
        snapshotFlow { events }
            .filterNotNull()
            .collect { mainViewModel.updateConfirmedEvents(customerState) }
    }
    LaunchedEffect(customerState) {
        snapshotFlow { customerState }
            .filterNotNull()
            .collect { mainViewModel.updateConfirmedEvents(it) }
    }

    if (confirmedEvents == null)
        LoadingBox()
    else
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(top = 8.dp),
        ) {
            val now = Calendar.getInstance().time.time

            stickyHeader {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)) {
                    Text(
                        text = stringResource(R.string.events_confirmed_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth(),
                    )
                }
            }
            items(
                confirmedEvents
                    // Filter past events
                    ?.filter { event -> event.eventDate?.time?.let { it >= now } ?: true }
                    ?: emptyList()
            ) { event ->
                EventItem(event, true)
            }
            stickyHeader {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)) {
                    Text(
                        text = stringResource(R.string.events_available_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth(),
                    )
                }
            }
            items(
                availableEvents
                // Filter past events
                ?.filter { event -> event.eventDate?.time?.let { it >= now } ?: true }
                ?: emptyList()
            ) { event ->
                EventItem(event, false)
            }
        }
}
