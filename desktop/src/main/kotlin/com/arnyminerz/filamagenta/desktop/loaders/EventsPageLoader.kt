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
import io.sentry.ISpan
import io.sentry.Sentry
import io.sentry.SpanStatus
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
            val transaction = Sentry.startTransaction("EventsPageLoader", "loadEvents")
            var span: ISpan? = null
            try {
                span = transaction.startChild(
                    "local_events",
                    "Load all the locally stored events."
                )
                Logger.d(TAG, "Loading cached events...")
                val eventsCacheFile = File(dataDir, "events.json")
                span.setMeasurement("events_size", eventsCacheFile.totalSpace)
                val eventsCache = eventsCacheFile.takeIf { it.exists() }
                    ?.inputStream()
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?.let { JSONArray(it) }
                    ?.mapObjects { Event.fromJSON(it) }
                span.setMeasurement("event_count", eventsCache?.size ?: -1)
                span.finish(SpanStatus.OK)

                span = transaction.startChild(
                    "remote_events",
                    "Load all the events available in the remote server."
                )
                Logger.d(TAG, "Fetching events from remote...")
                val events = RemoteCommerce.eventList(
                    eventsCache ?: emptyList()
                ) { (progress, max) ->
                    ui {
                        this@EventsPageLoader.progress.value = progress.toFloat() / max.toFloat()
                    }
                }
                span.setMeasurement("event_cache", eventsCache?.size ?: -1)
                span.setMeasurement("event_count", events.size)
                span.finish(SpanStatus.OK)

                Logger.d(TAG, "Updating events list in UI...")
                ui { this@EventsPageLoader.events.value = events; progress.value = null }

                span = transaction.startChild(
                    "store_cache",
                    "Store all the updated events into cache."
                )
                Logger.d(TAG, "Storing new events cache...")
                if (eventsCacheFile.exists()) eventsCacheFile.delete()
                eventsCacheFile.createNewFile()
                eventsCacheFile.outputStream().bufferedWriter()
                    .use { it.write(events.toJSON().toString()) }
                span.finish(SpanStatus.OK)
            } catch (e: Exception) {
                Logger.e(TAG, e, "Could not load all events.")

                span?.throwable = e
                span?.finish(SpanStatus.INTERNAL_ERROR)

                transaction.throwable = e
                transaction.status = SpanStatus.INTERNAL_ERROR
            } finally {
                transaction.finish()
            }
        }
    }
}
