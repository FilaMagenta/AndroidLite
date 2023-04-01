package com.arnyminerz.filamagenta.core.database.data.woo.event

import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializable
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer
import com.arnyminerz.filamagenta.core.utils.getLongOrNull
import org.json.JSONObject

data class Option(
    val variationId: Long?,
    val displayValue: String,
    val price: Double,
) : JsonSerializable {
    val value = displayValue.lowercase().replace(' ', '-')

    companion object : JsonSerializer<Option> {
        override fun fromJSON(json: JSONObject, vararg args: Any?): Option = Option(
            json.getLongOrNull("variationId"),
            json.getString("displayValue"),
            json.getDouble("price"),
        )
    }

    override fun toJSON(): JSONObject = JSONObject().apply {
        put("variationId", variationId)
        put("displayValue", displayValue)
        put("price", price)
    }
}
