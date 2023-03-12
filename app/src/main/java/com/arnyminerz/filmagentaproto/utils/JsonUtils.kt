package com.arnyminerz.filmagentaproto.utils

import com.arnyminerz.filmagentaproto.database.prototype.JsonSerializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
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

fun JSONObject.getIntOrNull(key: String): Int? = try {
    if (has(key))
        getInt(key)
    else
        null
} catch (e: JSONException) {
    null
}

fun JSONObject.getStringOrNull(key: String): String? = try {
    if (has(key))
        getString(key)
    else
        null
} catch (e: JSONException) {
    null
}

fun Iterable<JsonSerializable>.toJSON(): JSONArray = JSONArray().apply {
    for (serializable in this@toJSON)
        put(serializable.toJSON())
}

fun Iterable<String>.toJSONArray(): JSONArray = JSONArray().apply {
    for (serializable in this@toJSONArray)
        put(serializable)
}

fun <R> JSONArray.mapObjects(mapper: (JSONObject) -> R): List<R> = (0 until length()).map {
    mapper(getJSONObject(it))
}

fun JSONObject.getStringJSONArray(key: String): List<String> = getJSONArray(key).let { array ->
    (0 until array.length()).map { array.getString(it) }
}

private val dateFormatter: SimpleDateFormat
    get() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

/**
 * Gets the value date of the given field in the GMT timezone.
 * @throws NullPointerException If the date is not valid.
 * @throws JSONException If `this` doesn't contain the key [key] or it's not a String.
 * @see dateFormatter
 */
fun JSONObject.getDateGmt(key: String): Date = getString(key)
    .let {
        dateFormatter.timeZone = TimeZone.getTimeZone("GMT")
        dateFormatter.parse(it)!!
    }

/**
 * Gets the value date of the given field.
 * @throws NullPointerException If the date is not valid.
 * @throws JSONException If `this` doesn't contain the key [key] or it's not a String.
 * @see dateFormatter
 */
fun JSONObject.getDate(key: String): Date = getString(key)
    .let { dateFormatter.parse(it)!! }
