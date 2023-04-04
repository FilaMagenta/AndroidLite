package com.arnyminerz.filamagenta.core.resources

import java.io.InputStream
import java.io.OutputStream

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
    operator fun set(key: String, value: String) {
        if (readOnly) throw UnsupportedOperationException("You are trying to write into a read-only properties provider.")
        properties[key] = value
        writeProperties()
    }
}