package com.arnyminerz.filamagenta.core.database.data.woo.order

import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializable
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer
import org.json.JSONObject

data class OrderMetadata(
    val id: Long,
    val key: String,
    val value: String,
    val displayKey: String?,
    val displayValue: String?,
) : JsonSerializable {
    companion object : JsonSerializer<OrderMetadata> {
        override fun fromJSON(json: JSONObject, vararg args: Any?): OrderMetadata = OrderMetadata(
            json.getLong("id"),
            json.getString("key"),
            json.getString("value"),
            null,
            null,
        )
    }

    override fun toJSON(): JSONObject = JSONObject().apply {
        put("id", id)
        put("key", key)
        put("value", value)
        put("display_key", displayKey)
        put("display_value", displayValue)
    }
}
