package com.arnyminerz.filmagentaproto.database.data.woo

import androidx.annotation.StringDef
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arnyminerz.filamagenta.core.database.data.woo.EventProto
import com.arnyminerz.filamagenta.core.database.data.woo.InStock
import com.arnyminerz.filamagenta.core.database.data.woo.OnBackOrder
import com.arnyminerz.filamagenta.core.database.data.woo.OutOfStock
import java.util.Date

@StringDef(InStock, OutOfStock, OnBackOrder)
annotation class StockStatus

@Entity(
    tableName = "events",
    ignoredColumns = ["cutDescription"],
)
class Event(
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
    companion object {
        val EXAMPLE = Event(
            1, "Example Event", "example-event", "https://example.com",
            Date(1), Date(2), "This is the description of the event",
            "Reservas hasta el lunes 23 de marzo de 2023. 12 de abril de 2023",
            0.0, emptyList(), InStock, 120
        )
    }
}
