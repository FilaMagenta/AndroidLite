package com.arnyminerz.filamagenta.core.database.data.woo.event

import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializable
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer
import org.json.JSONObject

data class ShortAttribute(
    val id: Long,
    val name: String,
    val option: String,
) : JsonSerializable {
    companion object : JsonSerializer<ShortAttribute> {
        override fun fromJSON(json: JSONObject, vararg args: Any?): ShortAttribute =
            ShortAttribute(
                json.getLong("id"),
                json.getString("name"),
                json.getString("option")
            )
    }

    override fun toJSON(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("option", option)
    }
}
