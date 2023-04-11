package com.arnyminerz.filamagenta.desktop.utils

import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer
import com.arnyminerz.filamagenta.core.utils.mapObjects
import java.io.File
import org.json.JSONArray

/**
 * Reads the contents of the file (which should be a JSON array), and realizes each item into [T].
 */
fun <T: Any> File.readCacheList(serializer: JsonSerializer<T>) =
    this.takeIf { it.exists() }
        ?.inputStream()
        ?.bufferedReader()
        ?.use { it.readText() }
        ?.let { JSONArray(it) }
        ?.mapObjects { serializer.fromJSON(it) }
