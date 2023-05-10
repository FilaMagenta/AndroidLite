package com.arnyminerz.filamagenta.core.utils

import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializable
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.DateFormat
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

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

fun JSONObject.getBooleanOrNull(key: String): Boolean? = try {
    if (has(key))
        getBoolean(key)
    else
        null
} catch (e: JSONException) {
    null
}

fun JSONObject.getLongOrNull(key: String): Long? = try {
    if (has(key))
        getLong(key)
    else
        null
} catch (e: JSONException) {
    null
}

fun <T : Any> JSONObject.getObjectOrNull(key: String, serializer: com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer<T>): T? = try {
    if (has(key))
        getJSONObject(key).let { serializer.fromJSON(it) }
    else
        null
} catch (e: JSONException) {
    null
}

fun <T : Any> JSONObject.getObjectInlineOrNull(serializer: JsonSerializer<T>): T? = try {
    serializer.fromJSON(this)
} catch (e: JSONException) {
    null
}

fun Iterable<JsonSerializable>.toJSON(): JSONArray = JSONArray().apply {
    for (serializable in this@toJSON)
        put(serializable.toJSON())
}

fun Iterable<JSONObject>.toJSONObjectsArray(): JSONArray = JSONArray().apply {
    for (serializable in this@toJSONObjectsArray)
        put(serializable)
}

fun Iterable<String>.toJSONArray(): JSONArray = JSONArray().apply {
    for (serializable in this@toJSONArray)
        put(serializable)
}

inline fun <R> JSONArray.mapObjects(mapper: (JSONObject) -> R): List<R> = (0 until length()).map {
    mapper(getJSONObject(it))
}

inline fun <R> JSONArray.mapObjectsIndexed(mapper: (json: JSONObject, index: Int) -> R): List<R> =
    (0 until length()).map { index -> mapper(getJSONObject(index), index) }

fun JSONObject.getStringJSONArray(key: String): List<String> = getJSONArray(key).let { array ->
    (0 until array.length()).map { array.getString(it) }
}

fun <T : Any> JSONObject.getJSONArray(key: String, mapper: (Any) -> T): List<T> =
    getJSONArray(key).let { array ->
        (0 until array.length()).map { mapper(array.get(it)) }
    }

fun JSONObject.getJSONArrayOrNull(key: String) = try {
    if (has(key))
        getJSONArray(key)
    else
        null
} catch (e: JSONException) {
    null
}

fun <T : Any> JSONObject.getJSONArrayOrNull(key: String, serializer: com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer<T>): List<T>? =
    getJSONArrayOrNull(key)?.let { array ->
        (0 until array.length()).map { serializer.fromJSON(array.getJSONObject(it)) }
    }

fun <T : Any> JSONObject.toMap(converter: (Any) -> T): Map<String, T> =
    keys().asSequence().associateWith { converter(get(it)) }

private val dateFormatter: DateTimeFormatter
    get() = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

/**
 * Gets the value date of the given field in the GMT timezone.
 * @throws NullPointerException If the date is not valid.
 * @throws JSONException If `this` doesn't contain the key [key] or it's not a String.
 * @throws ParseException If the value at [key] cannot be parsed to a date.
 * @see dateFormatter
 */
fun JSONObject.getDateGmt(key: String): ZonedDateTime = getString(key)
    .let {
        val parsed = dateFormatter
            .withZone(TimeZone.getTimeZone("GMT").toZoneId())
            .parse(it)!!
        ZonedDateTime.from(parsed)
    }

/**
 * Gets the value date of the given field in the GMT timezone, or null if the field is not present,
 * or cannot be parsed into a date.
 * @see dateFormatter
 */
fun JSONObject.getDateGmtOrNull(key: String): ZonedDateTime? =
    try {
        getDateGmt(key)
    } catch (_: NullPointerException) {
        null
    } catch (_: JSONException) {
        null
    } catch (_: ParseException) {
        null
    }

/**
 * Gets the value date of the given field.
 * @throws NullPointerException If the date is not valid.
 * @throws JSONException If `this` doesn't contain the key [key] or it's not a String.
 * @see dateFormatter
 */
fun JSONObject.getDate(key: String): LocalDateTime = getString(key)
    .let { LocalDateTime.parse(it) }

/**
 * Puts the given date into the given [key] using the GMT timezone to be extracted later with
 * [getDateGmt] and [getDateGmtOrNull].
 */
fun JSONObject.putDateGmt(key: String, date: LocalDateTime?): JSONObject =
    put(key, date?.let { dateFormatter.format(it) })
