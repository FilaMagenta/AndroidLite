package com.arnyminerz.filamagenta.core.database.data.woo.event

import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializable
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer
import com.arnyminerz.filamagenta.core.utils.mapObjects
import com.arnyminerz.filamagenta.core.utils.toJSON
import org.json.JSONObject

data class Variation(
    val id: Long,
    val price: Double,
    val attributes: List<ShortAttribute>,
) : JsonSerializable {
    companion object : JsonSerializer<Variation> {
        override fun fromJSON(json: JSONObject, vararg args: Any?): Variation = Variation(
            json.getLong("id"),
            json.getString("price").toDoubleOrNull() ?: 0.0,
            json.getJSONArray("attributes").mapObjects { ShortAttribute.fromJSON(it) },
        )
    }

    override fun toJSON(): JSONObject = JSONObject().apply {
        put("id", id)
        put("price", price)
        put("attributes", attributes.toJSON())
    }
}
