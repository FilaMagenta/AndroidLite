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
import java.util.Date

@StringDef(PENDING, PROCESSING, ON_HOLD, COMPLETED, CANCELLED, REFUNDED, FAILED, TRASH)
annotation class Status

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey override val id: Long,
    override val status: String,
    override val currency: String,
    override val dateCreated: Date,
    override val dateModified: Date,
    override val total: Double,
    override val customerId: Long,
    @ColumnInfo(defaultValue = "null") override val payment: Payment?,
    override val items: List<Product>,
) : OrderProto(id, status, currency, dateCreated, dateModified, total, customerId, payment, items) {
    companion object
}
