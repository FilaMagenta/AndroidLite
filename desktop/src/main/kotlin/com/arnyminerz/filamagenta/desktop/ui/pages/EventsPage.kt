package com.arnyminerz.filamagenta.desktop.ui.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arnyminerz.filamagenta.desktop.loaders.EventsPageLoader
import com.arnyminerz.filamagenta.desktop.ui.components.list.EventItem

@Composable
fun ColumnScope.EventsPage(eventsPageLoader: EventsPageLoader = EventsPageLoader()) {
    val events by remember { eventsPageLoader.events }
    val progress by remember { eventsPageLoader.progress }

    LaunchedEffect(Unit) { eventsPageLoader.loadEvents() }

    progress?.let { LinearProgressIndicator(it, Modifier.fillMaxWidth()) }

    PageTitle("list.event.title")

    if (events == null)
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    else
        LazyVerticalGrid(
            columns = GridCells.Adaptive(400.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(events ?: emptyList()) { EventItem(it) }
        }
}
