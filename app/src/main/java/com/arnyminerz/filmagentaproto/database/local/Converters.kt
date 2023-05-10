package com.arnyminerz.filmagentaproto.database.local

import androidx.room.TypeConverter
import com.arnyminerz.filamagenta.core.data.Transaction
import com.arnyminerz.filamagenta.core.utils.toJSON
import java.sql.Date
import org.json.JSONArray
import java.time.LocalDate
import java.time.ZoneId

class Converters {
    @TypeConverter
    fun fromTransactionList(value: List<Transaction>?): String? =
        value?.toJSON()?.toString()

    @TypeConverter
    fun toTransactionList(value: String?): List<Transaction>? =
        value
            ?.let { JSONArray(it) }
            ?.let { array ->
                (0 until array.length())
                    .map { array.getJSONObject(it) }
                    .map { Transaction.fromJSON(it) }
            }

    @TypeConverter
    fun fromDate(value: Date?): Long? = value?.time

    @TypeConverter
    fun toDate(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun toLocalDate(value: Long?): LocalDate? = value?.let {
        Date(value).toInstant().atZone(ZoneId.of("UTC")).toLocalDate()
    }
}