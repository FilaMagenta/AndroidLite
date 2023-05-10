package com.arnyminerz.filamagenta.core.database.data.woo

import androidx.annotation.StringDef
import androidx.room.ColumnInfo
import androidx.room.Entity
import com.arnyminerz.filamagenta.core.Logger
import com.arnyminerz.filamagenta.core.database.data.woo.StockStatus.Companion.InStock
import com.arnyminerz.filamagenta.core.database.data.woo.StockStatus.Companion.OnBackOrder
import com.arnyminerz.filamagenta.core.database.data.woo.StockStatus.Companion.OutOfStock
import com.arnyminerz.filamagenta.core.database.data.woo.event.Attribute
import com.arnyminerz.filamagenta.core.database.data.woo.event.EventType
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer
import com.arnyminerz.filamagenta.core.utils.currentDateTime
import com.arnyminerz.filamagenta.core.utils.getDateGmt
import com.arnyminerz.filamagenta.core.utils.getJSONArrayOrNull
import com.arnyminerz.filamagenta.core.utils.lazyNullCacheable
import com.arnyminerz.filamagenta.core.utils.mapObjects
import com.arnyminerz.filamagenta.core.utils.now
import com.arnyminerz.filamagenta.core.utils.putDateGmt
import com.arnyminerz.filamagenta.core.utils.toJSON
import java.util.Calendar
import java.util.Date
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.time.temporal.IsoFields
import java.time.temporal.TemporalUnit

@StringDef(InStock, OutOfStock, OnBackOrder)
annotation class StockStatus {
    companion object {
        const val InStock = "instock"
        const val OutOfStock = "outofstock"
        const val OnBackOrder = "onbackorder"
    }
}

@Entity(tableName = "events", primaryKeys = ["id"])
data class Event(
    override val id: Long,
    val name: String,
    val slug: String,
    val permalink: String,
    val dateCreated: LocalDate,
    val dateModified: LocalDate,
    val description: String,
    val shortDescription: String,
    val price: Double,
    val attributes: List<Attribute>,
    @ColumnInfo(defaultValue = InStock) @StockStatus val stockStatus: String,
    @ColumnInfo(defaultValue = "0") val stockQuantity: Int,
) : WooClass(id) {
    companion object : JsonSerializer<Event> {
        private val untilKeyword = Regex(
            "Reservas? hasta el",
            setOf(
                RegexOption.MULTILINE,
                RegexOption.IGNORE_CASE,
            )
        )

        private val untilKeywordLine = Regex(
            "(<.*>)?Reservas? hasta el.*(</.*>)?",
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
            "^.*(lunes|martes|mi[ée]rcoles|jueves|viernes|s[aá]bado|domingo)? ?(d[ií]a)? ?\\d+ ?(de)? ?(enero|febrero|marzo|abril|mayo|junio|julio|agosto|septiembre|octubre|noviembre|diciembre) ?(del?)? ?(20\\d{2})?[ .,]*(\\d{1,2}:\\d{2})?.*$",
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

        val EXAMPLE = Event(
            1, "Example Event", "example-event", "https://example.com",
            LocalDate.ofYearDay(2023, 1), LocalDate.ofYearDay(2023, 1),
            "This is the description of the event",
            "Reservas hasta el lunes 23 de marzo de 2023. 12 de abril de 2023",
            0.0, emptyList(), InStock, 120
        )

        /**
         * Initializes the event from a JSONObject.
         *
         * **Note: [attributes] are set empty.**
         */
        override fun fromJSON(json: JSONObject, vararg args: Any?): Event = Event(
            json.getLong("id"),
            json.getString("name"),
            json.getString("slug"),
            json.getString("permalink"),
            json.getDateGmt("date_created_gmt").toLocalDate(),
            json.getDateGmt("date_modified_gmt").toLocalDate(),
            json.getString("description"),
            json.getString("short_description"),
            json.getDouble("price"),
            json.getJSONArrayOrNull("attributes")?.mapObjects { Attribute.fromJSON(it) } ?: emptyList(),
            json.getString("stock_status"),
            json.getInt("stock_quantity"),
        )
    }

    override fun toString(): String = id.toString()

    override fun toJSON(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("slug", slug)
        put("permalink", permalink)
        putDateGmt("date_created_gmt", dateCreated.atStartOfDay())
        putDateGmt("date_modified_gmt", dateModified.atStartOfDay())
        put("description", description)
        put("short_description", shortDescription)
        put("price", price)
        put("attributes", attributes.toJSON())
        put("stock_status", stockStatus)
        put("stock_quantity", stockQuantity)
    }

    val acceptsReservationsUntil: LocalDateTime? by lazy {
        shortDescription.let { desc ->
            val keywordFind = untilKeyword.find(desc)
            if (keywordFind == null) {
                Logger.d("Keyword not found in event $id")
                return@let null
            }
            val position = keywordFind.range.last
            if (position < 0) {
                Logger.d("Could not cut event $id. position=$position")
                return@let null
            }
            val lineBreak = desc.indexOf('\n', position)
            if (lineBreak < 0) {
                Logger.d("Could not cut event $id. lineBreak=$lineBreak")
                return@let null
            }

            var until = desc.substring(position + 1, lineBreak)
            for (weekday in days)
                until = until.replace(weekday, "")
            until = until.trim().trimEnd('.')

            val firstSpace = until.indexOf(' ')
            if (firstSpace < 0) {
                Logger.d("Could not find space in event $id. until=\"$until\"")
                return@let null
            }

            val dayString = until.substring(0, firstSpace)
            val day = dayString.toIntOrNull()
            if (day == null) {
                Logger.d("Could not parse number in event $id. dayString=\"$dayString\"")
                return@let null
            }

            until = until.replace(" de ", "")
            val eventMonth = months.indexOfFirst { until.contains(it, ignoreCase = true) } + 1

            var localDateTime = currentDateTime()

            // Reset seconds and nanos
            localDateTime = localDateTime.withSecond(0)
            localDateTime = localDateTime.withNano(0)

            // Update year
            val yearFind = yearRegex.find(until)
            if (yearFind != null) {
                val yearString = yearFind.value.toInt()
                localDateTime = localDateTime.withYear(yearString)
            } else {
                // If current month is greater than the month specified in event, increase year
                val currentMonth = localDateTime.monthValue
                if (currentMonth > eventMonth)
                    localDateTime = localDateTime.plusYears(1)
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
                    localDateTime = localDateTime.withHour(hours)
                    localDateTime = localDateTime.withMinute(minutes)
                    timeUpdated = true
                }
            }
            if (!timeUpdated) {
                localDateTime = localDateTime.withHour(23)
                localDateTime = localDateTime.withMinute(59)
            }

            localDateTime = localDateTime.withMonth(eventMonth)
            localDateTime = localDateTime.withDayOfMonth(day)

            localDateTime
        }
    }

    var cutDescription: String by lazyNullCacheable {
        shortDescription
            .replace(untilKeywordLine, "")
            .trim('\n', '\r', ' ')
    }
        private set

    val eventDate: LocalDateTime? by lazy {
        shortDescription
            .replace(untilKeywordLine, "")
            .trim('\n', '\r', ' ')
            .let { desc ->
                val find = dateRegex.find(desc) ?: return@let null
                val found = find.value

                var localDateTime = currentDateTime()

                // Reset seconds and nanos
                localDateTime = localDateTime.withSecond(0)
                localDateTime = localDateTime.withNano(0)

                // Update time
                var timeUpdated = false
                val timeFind = timeRegex.find(found)
                if (timeFind != null) {
                    val timeString = timeFind.value
                    val timeParts = timeString.split(':')
                    val hours = timeParts.getOrNull(0)?.toIntOrNull()
                    val minutes = timeParts.getOrNull(1)?.toIntOrNull()
                    if (hours != null && minutes != null) {
                        localDateTime = localDateTime.withHour(hours)
                        localDateTime = localDateTime.withMinute(minutes)
                        timeUpdated = true
                    }
                }
                if (!timeUpdated) {
                    localDateTime = localDateTime.withHour(0)
                    localDateTime = localDateTime.withMinute(0)
                }

                // Update day
                val day = found.split(" ")
                    .find { it.toIntOrNull() != null }
                    ?.toInt() ?: return@let null
                localDateTime = localDateTime.withDayOfMonth(day)

                // Update month
                val eventMonth = months.indexOfFirst { found.contains(it, ignoreCase = true) }
                    .takeIf { it >= 0 }
                    ?.plus(1) ?: return@let null
                localDateTime = localDateTime.withMonth(eventMonth)

                // Update year
                val yearFind = yearRegex.find(found)
                if (yearFind != null) {
                    val yearString = yearFind.value.toInt()
                    localDateTime = localDateTime.withYear(yearString)
                } else {
                    // If current month is greater than the month specified in event, increase year
                    val currentMonth = localDateTime.monthValue
                    if (currentMonth > eventMonth)
                        localDateTime = localDateTime.plusYears(1)
                }

                cutDescription = desc.replace(found, "").trim('\n', '\r')
                localDateTime
            }
    }

    val index: Int by lazy {
        Regex("^\\d+").find(name)
            ?.value
            ?.toIntOrNull() ?: Int.MAX_VALUE
    }

    val type: EventType? by lazy {
        EventType.values().find { type ->
            type.keywords.find { name.contains(it, true) } != null
        }
    }

    val title: String by lazy {
        Regex("^\\d+ ?").find(name)?.let {
            name.replace(it.value, "")
                .trimEnd(',', '.')
        } ?: name
    }

    /**
     * Returns whether the event has already passed or not. This is done by comparing the event's
     * date with the current date, and if 4 hours have passed, it's considered as true.
     */
    val hasPassed: Boolean by lazy {
        eventDate?.until(currentDateTime(), ChronoUnit.HOURS)?.let { it > -4 } ?: false
    }
}
