package com.arnyminerz.filamagenta.core.database.data.woo

import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer
import org.json.JSONObject

open class AvailablePaymentProto(
    override val id: Long,
    open val price: Double,
) : WooClass(id) {
    companion object : JsonSerializer<AvailablePaymentProto> {
        override fun fromJSON(json: JSONObject): AvailablePaymentProto = AvailablePaymentProto(
            json.getLong("id"),
            json.getDouble("price"),
        )
    }
}
