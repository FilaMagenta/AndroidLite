package com.arnyminerz.filmagentaproto.database.data.woo

import androidx.annotation.StringDef
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arnyminerz.filamagenta.core.database.data.woo.CANCELLED
import com.arnyminerz.filamagenta.core.database.data.woo.COMPLETED
import com.arnyminerz.filamagenta.core.database.data.woo.FAILED
import com.arnyminerz.filamagenta.core.database.data.woo.ON_HOLD
import com.arnyminerz.filamagenta.core.database.data.woo.OrderProto
import com.arnyminerz.filamagenta.core.database.data.woo.PENDING
import com.arnyminerz.filamagenta.core.database.data.woo.PROCESSING
import com.arnyminerz.filamagenta.core.database.data.woo.REFUNDED
import com.arnyminerz.filamagenta.core.database.data.woo.TRASH
import com.arnyminerz.filamagenta.core.database.data.woo.order.Payment
import com.arnyminerz.filamagenta.core.database.data.woo.order.Product
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer
import com.arnyminerz.filamagenta.core.utils.getDateGmt
import com.arnyminerz.filamagenta.core.utils.getObjectInlineOrNull
import com.arnyminerz.filamagenta.core.utils.getObjectOrNull
import com.arnyminerz.filamagenta.core.utils.mapObjects
import java.util.Date
import org.json.JSONObject

@StringDef(PENDING, PROCESSING, ON_HOLD, COMPLETED, CANCELLED, REFUNDED, FAILED, TRASH)
annotation class Status

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey override val id: Long,
    @Status override val status: String,
    override val currency: String,
    override val dateCreated: Date,
    override val dateModified: Date,
    override val total: Double,
    override val customerId: Long,
    @ColumnInfo(defaultValue = "null") override val payment: Payment?,
    override val items: List<Product>,
) : OrderProto(id, status, currency, dateCreated, dateModified, total, customerId, payment, items) {
    companion object: JsonSerializer<Order> {
        override fun fromJSON(json: JSONObject, vararg args: Any?): Order = Order(
            json.getLong("id"),
            json.getString("status"),
            json.getString("currency"),
            json.getDateGmt("date_created"),
            json.getDateGmt("date_modified"),
            json.getDouble("total"),
            json.getLong("customer_id"),
            json.getObjectInlineOrNull(Payment)
                ?: json.getObjectOrNull("payment", Payment),
            json.getJSONArray("line_items").mapObjects { Product.fromJSON(it) },
        )
    }
}
