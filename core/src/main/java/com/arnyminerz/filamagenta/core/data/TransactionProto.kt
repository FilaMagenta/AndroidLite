package com.arnyminerz.filamagenta.core.data

import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializable
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer
import com.arnyminerz.filamagenta.core.database.prototype.RemoteDataParser
import com.arnyminerz.filamagenta.core.utils.getBooleanOrNull
import java.sql.ResultSet
import java.util.Date
import org.json.JSONObject

open class TransactionProto(
    open val id: Long,
    open val idSocio: Long,
    open val date: Date,
    open val concept: String,
    /** The amount of units ordered */
    open val units: Int,
    /** The price of each unit */
    open val unitPrice: Double,
    /** The total price. Should be units*unitPrice */
    open val price: Double,
    /** If true, the money is entering the account, if false, it's an expense. */
    open val income: Boolean,
    /** Used for notifying about new transactions */
    open var notified: Boolean,
): JsonSerializable {
    companion object: JsonSerializer<TransactionProto>, RemoteDataParser<TransactionProto> {
        override fun fromJSON(json: JSONObject, vararg args: Any?): TransactionProto = TransactionProto(
            json.getLong("id"),
            json.getLong("id_socio"),
            Date(json.getLong("date")),
            json.getString("concept"),
            json.getInt("units"),
            json.getDouble("unit_price"),
            json.getDouble("price"),
            json.getBoolean("income"),
            json.getBooleanOrNull("notified") ?: false,
        )

        override fun parse(row: ResultSet): TransactionProto = TransactionProto(
            row.getLong("idApunte"),
            row.getLong("idSocio"),
            row.getDate("Fecha"),
            row.getString("Concepto"),
            row.getInt("Unidades"),
            row.getDouble("Precio"),
            row.getDouble("Importe"),
            row.getString("Tipo") == "I",
            false,
        )
    }

    override fun toJSON(): JSONObject = JSONObject().apply {
        put("id", id)
        put("id_socio", idSocio)
        put("date", date.time)
        put("concept", concept)
        put("units", units)
        put("unit_price", unitPrice)
        put("price", price)
        put("income", income)
        put("notified", notified)
    }
}

val Iterable<TransactionProto>.inwards: Double
    get() = filter { it.income }.sumOf { it.price }

val Iterable<TransactionProto>.outwards: Double
    get() = filter { !it.income }.sumOf { it.price }
