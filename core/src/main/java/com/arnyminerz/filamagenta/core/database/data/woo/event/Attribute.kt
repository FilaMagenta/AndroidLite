package com.arnyminerz.filamagenta.core.database.data.woo.event

import com.arnyminerz.filamagenta.core.database.data.woo.order.OrderMetadata
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializable
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer
import com.arnyminerz.filamagenta.core.utils.getObjectOrNull
import com.arnyminerz.filamagenta.core.utils.mapObjects
import com.arnyminerz.filamagenta.core.utils.toJSON
import org.json.JSONException
import org.json.JSONObject

data class Attribute(
    val id: Long,
    val name: String,
    val options: List<Option>,
    val variation: Variation?,
) : JsonSerializable {
    companion object : JsonSerializer<Attribute> {
        /**
         * Initializes the attribute from a JSONObject.
         *
         * **Note: [options] are set empty; [variation] is set to `null`**
         */
        override fun fromJSON(json: JSONObject, vararg args: Any?): Attribute = Attribute(
            json.getLong("id"),
            json.getString("name"),
            if (json.has("options"))
                json.getJSONArray("options")
                    .let { array ->
                        try {
                            array.mapObjects { Option.fromJSON(it) }
                        } catch (e: JSONException) {
                            // This is for compatibility with old versions
                            (0 until array.length())
                                .map { array.getString(it) }
                                .map { Option(null, it, 0.0) }
                        }
                    }
            else
                emptyList(),
            json.getObjectOrNull("variation", Variation),
        )
    }

    override fun toJSON(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("options", options.toJSON())
        put("variation", variation?.toJSON())
    }

    fun toMetadata(option: Option = options[0]): OrderMetadata = OrderMetadata(
        id = id,
        key = name.lowercase().replace(' ', '-'),
        displayKey = name,
        value = option.value,
        displayValue = option.displayValue,
    )
}
