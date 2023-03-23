package com.arnyminerz.filmagentaproto.database.data.woo

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.annotation.StringDef
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
import com.arnyminerz.filmagentaproto.security.AESEncryption
import com.arnyminerz.filmagentaproto.utils.getDateGmt
import com.arnyminerz.filmagentaproto.utils.mapObjects
import com.arnyminerz.filmagentaproto.utils.toJSON
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
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
            json.getJSONArray("line_items").mapObjects { Product.fromJSON(it) },
        )

        /**
         * Verifies the contents of a scanned QR code.
         * @param contents What was scanned from the QR code.
         * @return `null` if the code is not valid, the customer's name otherwise.
         */
        fun verifyQRCode(contents: String): String? {
            Log.d("QR_CODE", "Encoded QR code: $contents")
            val decoded = Base64.decode(contents, Base64.NO_WRAP).decodeToString()
            Log.d("QR_CODE", "Decoded QR code: $decoded")
            val json = JSONObject(decoded)
            if (!json.has("customer") || !json.has("confirmation_code") || !json.has("confirmation_hash")) {
                Log.d("QR_CODE", "JSON missing keys.")
                return null
            }
            val code = json.getString("confirmation_code")
            val decodedCode = Base64.decode(code, Base64.NO_WRAP).decodeToString()
            Log.d("QR_CODE", "Decoded code: $decodedCode")
            val hash = json.getString("confirmation_hash")
            val decryptedHash = AESEncryption.decrypt(hash)
            Log.d("QR_CODE", "Decrypted Hash: $decryptedHash")
            if (decodedCode != decryptedHash)
                return null
            return json.getString("customer")
        }
    }

    override fun toJSON(): JSONObject = JSONObject().apply {
        put("id", id)
        put("status", status)
        put("currency", currency)
        put("date_created", dateCreated)
        put("date_modified", dateModified)
        put("total", total)
        put("customer_id", customerId)
        put("line_items", items.toJSON())
    }

    /**
     * Obtains a QR code with the data of the order. Contains a QR code with the customer's name,
     * with key "customer"; and another key named "confirmation", that contains the confirmation
     * code to be scanned.
     */
    fun getQRCode(customer: Customer, size: Int = 400): Bitmap {
        val barcodeEncoder = BarcodeEncoder()
        val content = JSONObject().apply {
            put("customer", customer.firstName + " " + customer.lastName)
            val encodedCode = Base64.encodeToString(hashCode().toString().toByteArray(), Base64.NO_WRAP)
            put("confirmation_code", encodedCode)
            put("confirmation_hash", AESEncryption.encrypt(hashCode().toString()))
        }
        Log.d("QR_CODE", "Encoding to QR: $content")
        val encodedContents = Base64.encodeToString(content.toString().toByteArray(), Base64.NO_WRAP)
        Log.d("QR_CODE", "QR Base64: $encodedContents")
        return barcodeEncoder.encodeBitmap(encodedContents, BarcodeFormat.QR_CODE, size, size)
    }

    data class Product(
        val id: Long,
        val name: String,
        val productId: Long,
        val variationId: Long,
        val quantity: Long,
        val price: Double,
    ) : JsonSerializable {
        companion object : JsonSerializer<Product> {
            override fun fromJSON(json: JSONObject): Product = Product(
                json.getLong("id"),
                json.getString("name"),
                json.getLong("product_id"),
                json.getLong("variation_id"),
                json.getLong("quantity"),
                json.getDouble("price"),
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
        val displayKey: String,
        val displayValue: String,
    ) : JsonSerializable {
        override fun toJSON(): JSONObject = JSONObject().apply {
            put("id", id)
            put("key", key)
            put("value", value)
            put("display_key", displayKey)
            put("display_value", displayValue)
        }
    }
}
