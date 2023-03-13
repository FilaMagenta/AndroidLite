package com.arnyminerz.filmagentaproto.database.data.woo

import android.util.Log
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.arnyminerz.filmagentaproto.database.prototype.JsonSerializable
import com.arnyminerz.filmagentaproto.database.prototype.JsonSerializer
import com.arnyminerz.filmagentaproto.utils.getDateGmt
import com.arnyminerz.filmagentaproto.utils.getStringJSONArray
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

        private val dateRegex = Regex(
            "^.*(lunes|martes|mi[ée]rcoles|jueves|viernes|s[aá]bado|domingo) ?(d[ií]a)? ?\\d+ ?(de)? ?(enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre) ?(del?)? ?(20\\d{2})?[ .,]*(\\d{1,2}:\\d{2})?.*$",
            setOf(
                RegexOption.MULTILINE,
                RegexOption.IGNORE_CASE,
            ),
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
            "noviembre", "diciembre"
        )

        /**
         * Initializes the event from a JSONObject.
         *
         * **Note: [attributes] are set empty.**
         */
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
            emptyList(),
        )
    }

    data class Attribute(
        val id: Long,
        val name: String,
        val slug: String,
        val options: List<Option>,
    ) : JsonSerializable {
        companion object : JsonSerializer<Attribute> {
            /**
             * Initializes the attribute from a JSONObject.
             *
             * **Note: [options] are set empty.**
             */
            override fun fromJSON(json: JSONObject): Attribute = Attribute(
                json.getLong("id"),
                json.getString("name"),
                json.getString("slug"),
                if (json.has("options"))
                    json.getStringJSONArray("options").map {
                        Option(it)
                    }
                else
                    emptyList(),
            )
        }

        data class Option(val displayValue: String) {
            val value = displayValue.toLowerCase(Locale.current).replace(' ', '-')
        }

        override fun toJSON(): JSONObject = JSONObject().apply {
            put("id", id)
            put("name", name)
            put("slug", slug)
            put("options", options.map { it.displayValue }.toJSONArray())
        }

        fun toMetadata(option: Option = options[0]): Order.Metadata = Order.Metadata(
            id = id,
            key = slug,
            displayKey = name,
            value = option.value,
            displayValue = option.displayValue,
        )
    }

    override fun toString(): String = id.toString()

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
    var cutDescription: String = shortDescription
        .replace(untilKeywordLine, "")
        .trim('\n', '\r', ' ')
        private set

    @Ignore
    val eventDate: Date? = cutDescription.let { desc ->
        val find = dateRegex.find(desc) ?: return@let null
        val found = find.value

        val calendar = Calendar.getInstance()

        // Update time
        var timeUpdated = false
        val timeFind = timeRegex.find(found)
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
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
        }

        // Update day
        val day = found.split(" ")
            .find { it.toIntOrNull() != null }
            ?.toInt() ?: return@let null
        calendar.set(Calendar.DAY_OF_MONTH, day)

        // Update month
        val monthIndex = months.indexOfFirst { found.contains(it, ignoreCase = true) }
            .takeIf { it >= 0 } ?: return@let null
        calendar.set(Calendar.MONTH, monthIndex)

        // Update year
        val yearFind = yearRegex.find(found)
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

        cutDescription = cutDescription.replace(found, "")
        calendar.time
    }

    @Ignore
    val index: Int = Regex("^\\d+").find(name)
        ?.value
        ?.toIntOrNull() ?: Int.MAX_VALUE

    @Ignore
    val title: String = Regex("^\\d+ ?").find(name)?.let {
        name.replace(it.value, "")
    } ?: name
}
