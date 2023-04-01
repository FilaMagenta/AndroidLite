package com.arnyminerz.filmagentaproto.database.data.woo

import androidx.annotation.StringDef
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arnyminerz.filamagenta.core.database.data.woo.EventProto
import com.arnyminerz.filamagenta.core.database.data.woo.InStock
import com.arnyminerz.filamagenta.core.database.data.woo.OnBackOrder
import com.arnyminerz.filamagenta.core.database.data.woo.OutOfStock
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer
import com.arnyminerz.filamagenta.core.utils.getDateGmt
import java.util.Date
import org.json.JSONObject

@StringDef(InStock, OutOfStock, OnBackOrder)
annotation class StockStatus

@Entity(
    tableName = "events",
    ignoredColumns = ["cutDescription"],
)
data class Event(
    @PrimaryKey override val id: Long,
    override val name: String,
    override val slug: String,
    override val permalink: String,
    override val dateCreated: Date,
    override val dateModified: Date,
    override val description: String,
    override val shortDescription: String,
    override val price: Double,
    override val attributes: List<Attribute>,
    @ColumnInfo(defaultValue = InStock) @StockStatus override val stockStatus: String,
    @ColumnInfo(defaultValue = "0") override val stockQuantity: Int,
) : EventProto(
    id,
    name,
    slug,
    permalink,
    dateCreated,
    dateModified,
    description,
    shortDescription,
    price,
    attributes,
    stockStatus,
    stockQuantity
) {
    companion object: JsonSerializer<Event> {
        override fun fromJSON(json: JSONObject, vararg args: Any?): Event = Event(
            json.getLong("id"),
            json.getString("name"),
            json.getString("slug"),
            json.getString("permalink"),
            json.getDateGmt("date_created_gmt"),
            json.getDateGmt("date_modified_gmt"),
            json.getString("description"),
            json.getString("short_description"),
            json.getDouble("price"),
            (args[0] as? List<*>)?.map { it as Attribute } ?: emptyList(),
            json.getString("stock_status"),
            json.getInt("stock_quantity"),
        )

        val EXAMPLE = Event(
            1, "Example Event", "example-event", "https://example.com",
            Date(1), Date(2), "This is the description of the event",
            "Reservas hasta el lunes 23 de marzo de 2023. 12 de abril de 2023",
            0.0, emptyList(), InStock, 120
        )
    }
}
