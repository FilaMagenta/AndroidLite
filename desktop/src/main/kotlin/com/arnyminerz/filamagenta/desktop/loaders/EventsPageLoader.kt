package com.arnyminerz.filamagenta.desktop.loaders

import androidx.compose.runtime.mutableStateOf
import com.arnyminerz.filamagenta.core.Logger
import com.arnyminerz.filamagenta.core.database.data.woo.Event
import com.arnyminerz.filamagenta.core.utils.doAsync
import com.arnyminerz.filamagenta.core.utils.mapObjects
import com.arnyminerz.filamagenta.core.utils.toJSON
import com.arnyminerz.filamagenta.core.utils.ui
import com.arnyminerz.filamagenta.desktop.remote.RemoteCommerce
import com.arnyminerz.filamagenta.desktop.storage.dataDir
import java.io.File
import org.json.JSONArray

private const val TAG = "EventsPageLoader"

class EventsPageLoader {
    val progress = mutableStateOf<Float?>(null)

    val events = mutableStateOf<List<Event>?>(null)

    @Suppress("BlockingMethodInNonBlockingContext")
    fun loadEvents() {
        if (events.value != null) return
        doAsync {
            Logger.d(TAG, "Loading cached events...")
            val eventsCacheFile = File(dataDir, "events.json")
            val eventsCache = eventsCacheFile.takeIf { it.exists() }
                ?.inputStream()
                ?.bufferedReader()
                ?.use { it.readText() }
                ?.let { JSONArray(it) }
                ?.mapObjects { Event.fromJSON(it) }

            Logger.d(TAG, "Fetching events from remote...")
            val events = RemoteCommerce.eventList(eventsCache ?: emptyList()) { (progress, max) ->
                ui {
                    this@EventsPageLoader.progress.value = progress.toFloat() / max.toFloat()
                }
            }

            Logger.d(TAG, "Updating events list in UI...")
            ui { this@EventsPageLoader.events.value = events; progress.value = null }

            Logger.d(TAG, "Storing new events cache...")
            if (eventsCacheFile.exists()) eventsCacheFile.delete()
            eventsCacheFile.createNewFile()
            eventsCacheFile.outputStream().bufferedWriter().use { it.write(events.toJSON().toString()) }
        }
    }
}