package com.arnyminerz.filmagentaproto.database.data.woo

import android.util.Log
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.arnyminerz.filmagentaproto.database.prototype.JsonSerializable
import com.arnyminerz.filmagentaproto.database.prototype.JsonSerializer
import com.arnyminerz.filmagentaproto.utils.getDateGmt
import com.arnyminerz.filmagentaproto.utils.getStringJSONArray
import com.arnyminerz.filmagentaproto.utils.mapObjects
import com.arnyminerz.filmagentaproto.utils.toJSONArray
import java.util.Calendar
import java.util.Date
import org.json.JSONObject

@Entity(tableName = "events")
data class Event(
    @PrimaryKey val id: Long,
    val name: String,
    val slug: String,
    val permalink: String,
    val dateCreated: Date,
    val dateModified: Date,
    val description: String,
    val shortDescription: String,
    val price: Double,
    val attributes: List<Attribute>,
) {
    companion object : JsonSerializer<Event> {
        private val untilKeyword = Regex(
            "Reservas? hasta el",
            setOf(
                RegexOption.MULTILINE,
                RegexOption.IGNORE_CASE,
            )
        )

        private val untilKeywordLine = Regex(
            "<.*>Reservas? hasta el.*</.*>",
            setOf(
                RegexOption.MULTILINE,
                RegexOption.IGNORE_CASE,
            )
        )

        private val yearRegex = Regex(
            "20\\d\\d",
            setOf(
                RegexOption.MULTILINE,
                RegexOption.IGNORE_CASE,
            )
        )

        private val timeRegex = Regex(
            "\\d?\\d:\\d\\d",
            setOf(
                RegexOption.MULTILINE,
                RegexOption.IGNORE_CASE,
            )
        )

        private val days = listOf(
            "lunes",
            "martes",
            "miercoles",
            "miércoles",
            "jueves",
            "viernes",
            "sabado",
            "sábado",
            "domingo",
        )

        private val months = listOf(
            "enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "septiembre", "octubre",
            "noviembre"
        )

        override fun fromJSON(json: JSONObject): Event = Event(
            json.getLong("id"),
            json.getString("name"),
            json.getString("slug"),
            json.getString("permalink"),
            json.getDateGmt("date_created_gmt"),
            json.getDateGmt("date_modified_gmt"),
            json.getString("description"),
            json.getString("short_description"),
            json.getDouble("price"),
            json.getJSONArray("attributes").mapObjects { Attribute.fromJSON(it) },
        )
    }

    data class Attribute(
        val id: Long,
        val name: String,
        val position: Long,
        val visible: Boolean,
        val variation: Boolean,
        val options: List<String>,
    ) : JsonSerializable {
        companion object : JsonSerializer<Attribute> {
            override fun fromJSON(json: JSONObject): Attribute = Attribute(
                json.getLong("id"),
                json.getString("name"),
                json.getLong("position"),
                json.getBoolean("visible"),
                json.getBoolean("variation"),
                json.getStringJSONArray("options"),
            )
        }

        override fun toJSON(): JSONObject = JSONObject().apply {
            put("id", id)
            put("name", name)
            put("position", position)
            put("visible", visible)
            put("variation", variation)
            put("options", options.toJSONArray())
        }
    }

    @Ignore
    val acceptsReservationsUntil: Date? = shortDescription.let { desc ->
        val keywordFind = untilKeyword.find(desc)
        if (keywordFind == null) {
            Log.d("Event", "Keyword not found in event $id")
            return@let null
        }
        val position = keywordFind.range.last
        if (position < 0) {
            Log.d("Event", "Could not cut event $id. position=$position")
            return@let null
        }
        val lineBreak = desc.indexOf('\n', position)
        if (position < 0 || lineBreak < 0) {
            Log.d("Event", "Could not cut event $id. lineBreak=$lineBreak")
            return@let null
        }

        var until = desc.substring(position + 1, lineBreak)
        for (weekday in days)
            until = until.replace(weekday, "")
        until = until.trim().trimEnd('.')

        val firstSpace = until.indexOf(' ')
        if (firstSpace < 0) {
            Log.d("Event", "Could not find space in event $id. until=\"$until\"")
            return@let null
        }

        val dayString = until.substring(0, firstSpace)
        val day = dayString.toIntOrNull()
        if (day == null) {
            Log.d("Event", "Could not parse number in event $id. dayString=\"$dayString\"")
            return@let null
        }

        until = until.replace(" de ", "")
        val monthIndex = months.indexOfFirst { until.contains(it, ignoreCase = true) }

        val calendar = Calendar.getInstance()

        // Check if year is specified
        val yearFind = yearRegex.find(until)
        if (yearFind != null) {
            val yearString = yearFind.value
            yearString.toIntOrNull()?.let { year ->
                calendar.set(Calendar.YEAR, year)
            }
        } else {
            // If current month is greater than the month specified in event, increase year
            val currentMonth = calendar.get(Calendar.MONTH)
            if (currentMonth > monthIndex) {
                val year = calendar.get(Calendar.YEAR)
                calendar.set(Calendar.YEAR, year + 1)
            }
        }

        // Check if time is specified
        var timeUpdated = false
        val timeFind = timeRegex.find(until)
        if (timeFind != null) {
            val timeString = timeFind.value
            val timeParts = timeString.split(':')
            val hours = timeParts.getOrNull(0)?.toIntOrNull()
            val minutes = timeParts.getOrNull(1)?.toIntOrNull()
            if (hours != null && minutes != null) {
                calendar.set(Calendar.HOUR_OF_DAY, hours)
                calendar.set(Calendar.MINUTE, minutes)
                timeUpdated = true
            }
        }
        if (!timeUpdated) {
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
        }

        calendar.set(Calendar.MONTH, monthIndex)
        calendar.set(Calendar.DAY_OF_MONTH, day)

        calendar.time
    }

    @Ignore
    val cutDescription: String = shortDescription
        .replace(untilKeywordLine, "")
        .trim('\n', '\r', ' ')
}
