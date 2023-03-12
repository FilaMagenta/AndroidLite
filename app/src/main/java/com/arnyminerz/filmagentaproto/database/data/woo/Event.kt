package com.arnyminerz.filmagentaproto.database.data.woo

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arnyminerz.filmagentaproto.database.prototype.JsonSerializable
import com.arnyminerz.filmagentaproto.database.prototype.JsonSerializer
import com.arnyminerz.filmagentaproto.utils.getDateGmt
import com.arnyminerz.filmagentaproto.utils.getStringJSONArray
import com.arnyminerz.filmagentaproto.utils.mapObjects
import com.arnyminerz.filmagentaproto.utils.toJSONArray
import java.util.Date
import org.json.JSONObject

@Entity(tableName = "events")
data class Event(
    @PrimaryKey val id: Long,
    val name: String,
    val slug: String,
    val permalink: String,
    val dateCreated: Date,
    val dateModified: Date,
    val description: String,
    val shortDescription: String,
    val price: Double,
    val attributes: List<Attribute>,
) {
    companion object: JsonSerializer<Event> {
        override fun fromJSON(json: JSONObject): Event = Event(
            json.getLong("id"),
            json.getString("name"),
            json.getString("slug"),
            json.getString("permalink"),
            json.getDateGmt("date_created_gmt"),
            json.getDateGmt("date_modified_gmt"),
            json.getString("description"),
            json.getString("short_description"),
            json.getDouble("price"),
            json.getJSONArray("attributes").mapObjects { Attribute.fromJSON(it) },
        )
    }

    data class Attribute(
        val id: Long,
        val name: String,
        val position: Long,
        val visible: Boolean,
        val variation: Boolean,
        val options: List<String>,
    ): JsonSerializable {
        companion object: JsonSerializer<Attribute> {
            override fun fromJSON(json: JSONObject): Attribute = Attribute(
                json.getLong("id"),
                json.getString("name"),
                json.getLong("position"),
                json.getBoolean("visible"),
                json.getBoolean("variation"),
                json.getStringJSONArray("options"),
            )
        }

        override fun toJSON(): JSONObject = JSONObject().apply {
            put("id", id)
            put("name", name)
            put("position", position)
            put("visible", visible)
            put("variation", variation)
            put("options", options.toJSONArray())
        }
    }
}
