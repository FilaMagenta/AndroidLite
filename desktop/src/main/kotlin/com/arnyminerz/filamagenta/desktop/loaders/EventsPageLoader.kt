package com.arnyminerz.filamagenta.desktop.loaders

import androidx.compose.runtime.mutableStateOf
import com.arnyminerz.filamagenta.core.database.data.woo.Event
import com.arnyminerz.filamagenta.desktop.remote.RemoteCommerce

class EventsPageLoader: Loader() {
    val progress = mutableStateOf<Float?>(null)

    val events = CachedList("events", Event.Companion)

    fun loadEvents() = loadList(events, progress) { cache, progressCallback ->
        RemoteCommerce.eventList(cache, progressCallback)
    }
}
