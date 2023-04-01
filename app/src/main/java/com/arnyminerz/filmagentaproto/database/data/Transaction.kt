package com.arnyminerz.filmagentaproto.database.data

import androidx.room.Entity
import com.arnyminerz.filamagenta.core.data.TransactionProto
import java.util.Date

@Entity(tableName = "transactions", primaryKeys = ["id"])
data class Transaction(
    override val id: Long,
    override val idSocio: Long,
    override val date: Date,
    override val concept: String,
    /** The amount of units ordered */
    override val units: Int,
    /** The price of each unit */
    override val unitPrice: Double,
    /** The total price. Should be units*unitPrice */
    override val price: Double,
    /** If true, the money is entering the account, if false, it's an expense. */
    override val income: Boolean,
    /** Used for notifying about new transactions */
    override var notified: Boolean,
): TransactionProto(id, idSocio, date, concept, units, unitPrice, price, income, notified)
