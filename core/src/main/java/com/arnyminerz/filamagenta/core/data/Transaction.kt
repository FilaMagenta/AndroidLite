package com.arnyminerz.filamagenta.core.data

import androidx.room.Entity
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializable
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer
import com.arnyminerz.filamagenta.core.database.prototype.RemoteDataParser
import com.arnyminerz.filamagenta.core.utils.getBooleanOrNull
import java.sql.ResultSet
import java.util.Date
import org.json.JSONObject

@Entity(tableName = "transactions", primaryKeys = ["id"])
data class Transaction(
    val id: Long,
    val idSocio: Long,
    val date: Date,
    val concept: String,
    /** The amount of units ordered */
    val units: Int,
    /** The price of each unit */
    val unitPrice: Double,
    /** The total price. Should be units*unitPrice */
    val price: Double,
    /** If true, the money is entering the account, if false, it's an expense. */
    val income: Boolean,
    /** Used for notifying about new transactions */
    var notified: Boolean,
): JsonSerializable {
    companion object: JsonSerializer<Transaction>, RemoteDataParser<Transaction> {
        override fun fromJSON(json: JSONObject, vararg args: Any?): Transaction = Transaction(
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

        override fun parse(row: ResultSet): Transaction = Transaction(
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

val Iterable<Transaction>.inwards: Double
    get() = filter { it.income }.sumOf { it.price }

val Iterable<Transaction>.outwards: Double
    get() = filter { !it.income }.sumOf { it.price }
