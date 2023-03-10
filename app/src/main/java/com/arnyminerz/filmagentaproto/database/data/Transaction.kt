package com.arnyminerz.filmagentaproto.database.data

import androidx.room.Ignore
import com.arnyminerz.filmagentaproto.database.prototype.JsonSerializable
import com.arnyminerz.filmagentaproto.database.prototype.JsonSerializer
import com.arnyminerz.filmagentaproto.utils.getDoubleOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONObject

data class Transaction(
    val date: String,
    val description: String,
    val units: Long,
    val enters: Double?,
    val exits: Double?,
): JsonSerializable {
    companion object: JsonSerializer<Transaction> {
        override fun fromJSON(json: JSONObject): Transaction = Transaction(
            json.getString("date"),
            json.getString("description"),
            json.getLong("units"),
            json.getDoubleOrNull("enters"),
            json.getDoubleOrNull("exits"),
        )
    }

    override fun toJSON(): JSONObject = JSONObject().apply {
        put("date", date)
        put("description", description)
        put("units", units)
        put("enters", enters)
        put("exits", exits)
    }

    @Ignore
    val timestamp: Date? = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        .parse(date)
}
