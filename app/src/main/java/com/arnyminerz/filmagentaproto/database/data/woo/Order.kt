package com.arnyminerz.filmagentaproto.database.data.woo

import androidx.annotation.StringDef
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arnyminerz.filmagentaproto.database.data.woo.Status.Companion.CANCELLED
import com.arnyminerz.filmagentaproto.database.data.woo.Status.Companion.COMPLETED
import com.arnyminerz.filmagentaproto.database.data.woo.Status.Companion.FAILED
import com.arnyminerz.filmagentaproto.database.data.woo.Status.Companion.ON_HOLD
import com.arnyminerz.filmagentaproto.database.data.woo.Status.Companion.PENDING
import com.arnyminerz.filmagentaproto.database.data.woo.Status.Companion.PROCESSING
import com.arnyminerz.filmagentaproto.database.data.woo.Status.Companion.REFUNDED
import com.arnyminerz.filmagentaproto.database.data.woo.Status.Companion.TRASH
import com.arnyminerz.filmagentaproto.database.prototype.JsonSerializable
import com.arnyminerz.filmagentaproto.database.prototype.JsonSerializer
import com.arnyminerz.filmagentaproto.security.Hashing
import com.arnyminerz.filmagentaproto.utils.getBooleanOrNull
import com.arnyminerz.filmagentaproto.utils.getDateGmt
import com.arnyminerz.filmagentaproto.utils.getDateGmtOrNull
import com.arnyminerz.filmagentaproto.utils.getJSONArrayOrNull
import com.arnyminerz.filmagentaproto.utils.getObjectInlineOrNull
import com.arnyminerz.filmagentaproto.utils.getObjectOrNull
import com.arnyminerz.filmagentaproto.utils.getStringOrNull
import com.arnyminerz.filmagentaproto.utils.mapObjects
import com.arnyminerz.filmagentaproto.utils.putDateGmt
import com.arnyminerz.filmagentaproto.utils.toJSON
import java.util.Date
import org.json.JSONObject

@StringDef(PENDING, PROCESSING, ON_HOLD, COMPLETED, CANCELLED, REFUNDED, FAILED, TRASH)
annotation class Status {
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

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey override val id: Long,
    val status: String,
    val currency: String,
    val dateCreated: Date,
    val dateModified: Date,
    val total: Double,
    val customerId: Long,
    @ColumnInfo(defaultValue = "null") val payment: Payment?,
    val items: List<Product>,
) : JsonSerializable, WooClass(id) {
    companion object : JsonSerializer<Order> {
        override fun fromJSON(json: JSONObject): Order = Order(
            json.getLong("id"),
            json.getString("status"),
            json.getString("currency"),
            json.getDateGmt("date_created"),
            json.getDateGmt("date_modified"),
            json.getDouble("total"),
            json.getLong("customer_id"),
            json.getObjectInlineOrNull(Payment.Companion)
                ?: json.getObjectOrNull("payment", Payment.Companion),
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

    data class Product(
        val id: Long,
        val name: String,
        val productId: Long,
        val variationId: Long,
        val quantity: Long,
        val price: Double,
        val metadata: List<Metadata>?,
    ) : JsonSerializable {
        companion object : JsonSerializer<Product> {
            override fun fromJSON(json: JSONObject): Product = Product(
                json.getLong("id"),
                json.getString("name"),
                json.getLong("product_id"),
                json.getLong("variation_id"),
                json.getLong("quantity"),
                json.getDouble("price"),
                json.getJSONArrayOrNull("meta_data", Metadata.Companion)
            )
        }

        override fun toJSON(): JSONObject = JSONObject().apply {
            put("id", id)
            put("name", name)
            put("product_id", productId)
            put("variation_id", variationId)
            put("quantity", quantity)
            put("price", price)
        }
    }

    data class Metadata(
        val id: Long,
        val key: String,
        val value: String,
        val displayKey: String?,
        val displayValue: String?,
    ) : JsonSerializable {
        companion object: JsonSerializer<Metadata> {
            override fun fromJSON(json: JSONObject): Metadata = Metadata(
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

    data class Payment(
        val paid: Boolean?,
        val paymentMethod: String?,
        val paymentMethodTitle: String?,
        val transactionId: String?,
        val date: Date?,
    ) : JsonSerializable {
        companion object : JsonSerializer<Payment> {
            override fun fromJSON(json: JSONObject): Payment = Payment(
                json.getBooleanOrNull("paid"),
                json.getStringOrNull("payment_method")?.takeIf { it.isNotEmpty() },
                json.getStringOrNull("payment_method_title")?.takeIf { it.isNotEmpty() },
                json.getStringOrNull("transaction_id")?.takeIf { it.isNotEmpty() },
                json.getDateGmtOrNull("date_paid_gmt"),
            )
        }

        override fun toJSON(): JSONObject = JSONObject()
            .put("paid", paid)
            .put("payment_method", paymentMethod)
            .put("payment_method_title", paymentMethodTitle)
            .put("transaction_id", transactionId)
            .putDateGmt("date_paid_gmt", date)

        /** `true` if any of the fields is not null. date is not considered */
        val any: Boolean = paid == true ||
                listOf(paid, paymentMethod, paymentMethodTitle, transactionId)
                    .any { it != null }
    }

    /** Provides a hash that uniquely identifies the Order. */
    val hash: String by lazy {
        val str =
            "$id;$status;$currency;${dateCreated.time};${dateModified.time};$total;$customerId;${items.map { "${it.id}:${it.productId}:${it.variationId}:${it.quantity}" }}"
        Hashing.sha256(str)
    }
}
