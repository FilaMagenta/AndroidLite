package com.arnyminerz.filmagentaproto.utils

import com.arnyminerz.filmagentaproto.database.prototype.JsonSerializable
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

fun JSONObject.getDoubleOrNull(key: String): Double? = try {
    if (has(key))
        getDouble(key)
    else
        null
} catch (e: JSONException) {
    null
}

fun Iterable<JsonSerializable>.toJSON(): JSONArray = JSONArray().apply {
    for (serializable in this@toJSON)
        put(serializable.toJSON())
}
