package com.arnyminerz.filamagenta.core.database.data.woo.order

import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializable
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer
import com.arnyminerz.filamagenta.core.utils.getJSONArrayOrNull
import org.json.JSONObject

data class Product(
    val id: Long,
    val name: String,
    val productId: Long,
    val variationId: Long,
    val quantity: Long,
    val price: Double,
    val metadata: List<OrderMetadata>?,
) : JsonSerializable {
    companion object :
        JsonSerializer<Product> {
        override fun fromJSON(json: JSONObject, vararg args: Any?): Product = Product(
            json.getLong("id"),
            json.getString("name"),
            json.getLong("product_id"),
            json.getLong("variation_id"),
            json.getLong("quantity"),
            json.getDouble("price"),
            json.getJSONArrayOrNull("meta_data", OrderMetadata)
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
