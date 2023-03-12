package com.arnyminerz.filmagentaproto.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import com.arnyminerz.filmagentaproto.activity.MainActivity
import com.arnyminerz.filmagentaproto.ui.components.EventItem
import com.arnyminerz.filmagentaproto.ui.components.LoadingBox

@Composable
fun EventsScreen(mainViewModel: MainActivity.MainViewModel) {
    val customerState by mainViewModel.customer.collectAsState(initial = null)
    val events by mainViewModel.events.observeAsState(emptyList())

    customerState?.let { customer ->
        LazyColumn(Modifier.fillMaxSize()) {
            items(events) {event ->
                val confirmed by mainViewModel.isConfirmed(event, customer).observeAsState()
                EventItem(event, confirmed ?: false)
            }
        }
    } ?: LoadingBox()
}
