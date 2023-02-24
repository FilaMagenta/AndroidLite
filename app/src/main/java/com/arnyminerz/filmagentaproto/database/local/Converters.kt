package com.arnyminerz.filmagentaproto.database.local

import androidx.room.TypeConverter
import com.arnyminerz.filmagentaproto.database.data.Transaction
import com.arnyminerz.filmagentaproto.utils.toJSON
import java.sql.Date
import org.json.JSONArray

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
}