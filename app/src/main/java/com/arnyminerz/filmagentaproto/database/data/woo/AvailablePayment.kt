package com.arnyminerz.filmagentaproto.database.data.woo

import androidx.room.Entity
import com.arnyminerz.filamagenta.core.database.data.woo.AvailablePaymentProto

@Entity(
    tableName = "available_payments",
    primaryKeys = ["id"]
)
data class AvailablePayment(
    override val id: Long,
    override val price: Double,
): AvailablePaymentProto(id, price)
