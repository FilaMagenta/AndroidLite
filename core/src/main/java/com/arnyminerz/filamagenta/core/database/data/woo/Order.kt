package com.arnyminerz.filamagenta.core.database.data.woo

import androidx.annotation.StringDef
import androidx.room.ColumnInfo
import androidx.room.Entity
import com.arnyminerz.filamagenta.core.database.data.woo.OrderStatus.Companion.CANCELLED
import com.arnyminerz.filamagenta.core.database.data.woo.OrderStatus.Companion.COMPLETED
import com.arnyminerz.filamagenta.core.database.data.woo.OrderStatus.Companion.FAILED
import com.arnyminerz.filamagenta.core.database.data.woo.OrderStatus.Companion.ON_HOLD
import com.arnyminerz.filamagenta.core.database.data.woo.OrderStatus.Companion.PENDING
import com.arnyminerz.filamagenta.core.database.data.woo.OrderStatus.Companion.PROCESSING
import com.arnyminerz.filamagenta.core.database.data.woo.OrderStatus.Companion.REFUNDED
import com.arnyminerz.filamagenta.core.database.data.woo.OrderStatus.Companion.TRASH
import com.arnyminerz.filamagenta.core.database.data.woo.order.Payment
import com.arnyminerz.filamagenta.core.database.data.woo.order.Product
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializable
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer
import com.arnyminerz.filamagenta.core.security.Hashing
import com.arnyminerz.filamagenta.core.utils.getDateGmt
import com.arnyminerz.filamagenta.core.utils.getObjectInlineOrNull
import com.arnyminerz.filamagenta.core.utils.getObjectOrNull
import com.arnyminerz.filamagenta.core.utils.mapObjects
import com.arnyminerz.filamagenta.core.utils.toJSON
import java.util.Date
import org.json.JSONObject

@StringDef(PENDING, PROCESSING, ON_HOLD, COMPLETED, CANCELLED, REFUNDED, FAILED, TRASH)
annotation class OrderStatus {
    companion object {
        const val PENDING = "pending"
        const val PROCESSING = "processing"
        const val ON_HOLD = "on-hold"
        const val COMPLETED = "completed"
        const val CANCELLED = "cancelled"
        const val REFUNDED = "refunded"
        const val FAILED = "failed"
        const val TRASH = "trash"
    }
}

@Entity(tableName = "orders", primaryKeys = ["id"])
data class Order(
    override val id: Long,
    @OrderStatus val status: String,
    val currency: String,
    val dateCreated: Date,
    val dateModified: Date,
    val total: Double,
    val customerId: Long,
    @ColumnInfo(defaultValue = "null") val payment: Payment?,
    val items: List<Product>,
) : JsonSerializable, WooClass(id) {
    companion object : JsonSerializer<Order> {
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

    override fun toJSON(): JSONObject = JSONObject().apply {
        put("id", id)
        put("status", status)
        put("currency", currency)
        put("date_created", dateCreated)
        put("date_modified", dateModified)
        put("total", total)
        put("customer_id", customerId)
        put("payment", payment?.toJSON())
        put("line_items", items.toJSON())
    }

    /** Provides a hash that uniquely identifies the Order. */
    val hash: String by lazy {
        val str =
            "$id;$status;$currency;${dateCreated.time};${dateModified.time};$total;$customerId;${items.map { "${it.id}:${it.productId}:${it.variationId}:${it.quantity}" }}"
        Hashing.sha256(str)
    }
}
