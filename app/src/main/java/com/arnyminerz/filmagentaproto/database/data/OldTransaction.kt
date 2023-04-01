@file:Suppress("DEPRECATION")

package com.arnyminerz.filmagentaproto.database.data

import androidx.room.Ignore
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializable
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer
import com.arnyminerz.filamagenta.core.utils.getBooleanOrNull
import com.arnyminerz.filamagenta.core.utils.getDoubleOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONObject

@Deprecated("Replace with Transaction")
data class OldTransaction(
    val date: String,
    val description: String,
    val units: Long,
    val enters: Double?,
    val exits: Double?,
    val notified: Boolean,
): JsonSerializable {
    companion object: JsonSerializer<OldTransaction> {
        override fun fromJSON(json: JSONObject, vararg args: Any?): OldTransaction = OldTransaction(
            json.getString("date"),
            json.getString("description"),
            json.getLong("units"),
            json.getDoubleOrNull("enters"),
            json.getDoubleOrNull("exits"),
            json.getBooleanOrNull("notified") ?: true,
        )
    }

    override fun toJSON(): JSONObject = JSONObject().apply {
        put("date", date)
        put("description", description)
        put("units", units)
        put("enters", enters)
        put("exits", exits)
        put("notified", notified)
    }

    @Ignore
    val timestamp: Date? = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        .parse(date)
}
