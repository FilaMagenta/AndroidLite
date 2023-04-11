package com.arnyminerz.filamagenta.desktop.loaders

import androidx.compose.runtime.MutableState
import com.arnyminerz.filamagenta.core.Logger
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializable
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer
import com.arnyminerz.filamagenta.core.utils.doAsync
import com.arnyminerz.filamagenta.core.utils.toJSON
import com.arnyminerz.filamagenta.core.utils.ui
import com.arnyminerz.filamagenta.desktop.storage.dataDir
import com.arnyminerz.filamagenta.desktop.utils.readCacheList
import io.sentry.ISpan
import io.sentry.Sentry
import io.sentry.SpanStatus
import java.io.File

abstract class Loader {
    abstract inner class CacheFile<T>(val name: String) : MutableState<T> {
        private val file = File(dataDir, "$name.json")

        protected abstract fun read(): T

        /**
         * Converts [value] into a String. Must not return `null` if [value] is null.
         */
        protected abstract fun toStringConverter(value: T?): String?

        override var value: T
            get() = component1()
            set(value) {
                component2()(value)
            }

        override fun component1(): T = read()

        override fun component2(): (T?) -> Unit = { list ->
            if (list != null) {
                file.outputStream().bufferedWriter().use { stream ->
                    stream.write(toStringConverter(list)!!)
                }
            }
        }
    }

    inner class CachedList<T : JsonSerializable>(
        name: String,
        private val serializer: JsonSerializer<T>
    ) : CacheFile<List<T>?>(name) {
        private val file = File(dataDir, "$name.json")

        val totalSpace: Long
            get() = file.totalSpace

        override fun read(): List<T>? = file.readCacheList(serializer)

        override fun toStringConverter(value: List<T>?): String? = value?.toJSON()?.toString()
    }

    protected fun <T : JsonSerializable> loadList(
        cache: CachedList<T>,
        progressState: MutableState<Float?>?,
        loader: suspend (cache: List<T>, progressCallback: suspend (Pair<Int, Int>) -> Unit) -> List<T>,
    ) {
        if (cache.value != null) return
        doAsync {
            val transaction = Sentry.startTransaction("Loader", cache.name)
            var span: ISpan? = null
            try {
                span = transaction.startChild(
                    "local_${cache.name}",
                    "Load all the locally stored ${cache.name}."
                )
                Logger.d("Loading cached events...")
                span.setMeasurement("events_size", cache.totalSpace)
                val cacheList = cache.value
                span.setMeasurement("event_count", cacheList?.size ?: -1)
                span.finish(SpanStatus.OK)

                span = transaction.startChild(
                    "remote_${cache.name}",
                    "Load all the ${cache.name} available in the remote server."
                )
                Logger.d("Fetching ${cache.name} from remote...")
                val items = loader(cacheList ?: emptyList()) { (progress, max) ->
                    ui { progressState?.value = progress.toFloat() / max.toFloat() }
                }
                span.setMeasurement("cache", cacheList?.size ?: -1)
                span.setMeasurement("count", items.size)
                span.finish(SpanStatus.OK)

                Logger.d("Updating ${cache.name} list in UI...")
                ui { cache.value = items; progressState?.value = null }
            } catch (e: Exception) {
                Logger.e(e, "Could not load all ${cache.name}.")

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
