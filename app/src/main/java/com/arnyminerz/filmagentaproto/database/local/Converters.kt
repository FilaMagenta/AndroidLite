package com.arnyminerz.filmagentaproto.database.local

import androidx.room.TypeConverter
import com.arnyminerz.filamagenta.core.data.TransactionProto
import com.arnyminerz.filamagenta.core.utils.toJSON
import com.arnyminerz.filmagentaproto.database.data.Transaction
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
                    .map { TransactionProto.fromJSON(it) as Transaction }
            }

    @TypeConverter
    fun fromDate(value: Date?): Long? = value?.time

    @TypeConverter
    fun toDate(value: Long?): Date? = value?.let { Date(it) }
}