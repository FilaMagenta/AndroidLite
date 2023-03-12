package com.arnyminerz.filmagentaproto.database.data.woo

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arnyminerz.filmagentaproto.database.prototype.JsonSerializer
import org.json.JSONObject

@Entity(tableName = "available_payments")
data class AvailablePayment(
    @PrimaryKey val id: Long,
    val price: Double,
) {
    companion object: JsonSerializer<AvailablePayment> {
        override fun fromJSON(json: JSONObject): AvailablePayment = AvailablePayment(
            json.getLong("id"),
            json.getDouble("price"),
        )
    }
}
