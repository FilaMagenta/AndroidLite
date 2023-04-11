package com.arnyminerz.filamagenta.core.resources

import com.arnyminerz.filamagenta.core.Logger
import com.arnyminerz.filamagenta.core.utils.doAsync
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.channelFlow

abstract class PropertiesProvider(private val readOnly: Boolean) {
    /**
     * The stream for reading all the properties. Should never be null, or [get] will always
     * return null.
     */
    protected abstract fun getInputStream(): InputStream?

    /**
     * The stream for writing the properties back to the source file. Should be `null` if [readOnly]
     * is `true`, but will be ignored.
     */
    protected abstract fun getOutputStream(): OutputStream?

    private val collectorsLock = ReentrantLock()

    private val collectors: MutableMap<String, List<suspend CoroutineScope.(String?) -> Unit>> = mutableMapOf()

    private val properties: MutableMap<String, String> by lazy {
        getInputStream()
            ?.bufferedReader()
            ?.use { it.readText() }
            ?.split('\n')
            ?.associate {
                val pieces = it.split('=')
                pieces[0] to pieces.subList(1, pieces.size).joinToString("=")
            }
            ?.filter { (key, value) -> key.isNotBlank() && value.isNotBlank() }
            ?.toMutableMap() ?: mutableMapOf()
    }

    /**
     * Writes all the properties stored in [properties] using [getOutputStream].
     * @throws UnsupportedOperationException If [readOnly] is `true`.
     */
    private fun writeProperties() {
        if (readOnly) throw UnsupportedOperationException("You are trying to write into a read-only properties provider.")

        val contents = properties.toList()
            .filter { (key, value) -> key.isNotBlank() && value.isNotBlank() }
            .joinToString("\n") { (key, value) -> "$key=$value" }
            .toByteArray(Charsets.UTF_8)
        getOutputStream()?.use { it.write(contents) }
    }

    /**
     * Returns the current value of the property stored at [key].
     */
    operator fun get(key: String): String? = properties[key]

    /**
     * Updates the property at [key] with the given [value]. Then writes all the properties back
     * again into the properties file.
     * @throws UnsupportedOperationException If [readOnly] is `true`.
     */
    operator fun set(key: String, value: String?) {
        setMemory(key, value)
        writeProperties()
    }

    /**
     * Updates the current value of the property at [key] to [value], but doesn't store its value
     * in the output file. Also calls all the listeners added with [getLive], for example.
     */
    fun setMemory(key: String, value: String?) {
        if (readOnly) throw UnsupportedOperationException("You are trying to write into a read-only properties provider.")
        if (value == null)
            properties.remove(key)
        else
            properties[key] = value
        doAsync {
            collectorsLock.lock()
            collectors[key]?.let { callbacks ->
                Logger.d("Running ${callbacks.size} callbacks for property \"$key\"")
                for (callback in callbacks) callback(value)
            }
            collectorsLock.unlock()
        }
    }

    /**
     * Resets the value of the property [key] to `null`.
     */
    fun clear(key: String) = set(key, null)

    fun getLive(key: String) = channelFlow {
        // Emit the initial value
        send(get(key))
        // Add to collectors for collecting all the new values
        collectorsLock.withLock {
            val list = (collectors[key] ?: emptyList()).toMutableList()
            list.add {
                Logger.v("Got new data for \"$key\": $it")
                send(it)
            }
            Logger.v("Adding new collector for $key")
            collectors[key] = list
        }
        awaitCancellation()
    }
}