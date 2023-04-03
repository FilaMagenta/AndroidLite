package com.arnyminerz.filamagenta.core.database.data.woo

import androidx.room.Entity
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer
import org.json.JSONObject

@Entity(tableName = "available_payments", primaryKeys = ["id"])
data class AvailablePayment(
    override val id: Long,
    val price: Double,
) : WooClass(id) {
    companion object : JsonSerializer<AvailablePayment> {
        override fun fromJSON(json: JSONObject, vararg args: Any?): AvailablePayment = AvailablePayment(
            json.getLong("id"),
            json.getDouble("price"),
        )
    }
}
