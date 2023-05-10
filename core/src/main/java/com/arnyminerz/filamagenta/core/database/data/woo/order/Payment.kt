package com.arnyminerz.filamagenta.core.database.data.woo.order

import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializable
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer
import com.arnyminerz.filamagenta.core.utils.getBooleanOrNull
import com.arnyminerz.filamagenta.core.utils.getDateGmtOrNull
import com.arnyminerz.filamagenta.core.utils.getStringOrNull
import com.arnyminerz.filamagenta.core.utils.putDateGmt
import java.util.Date
import org.json.JSONObject
import java.time.LocalDate

data class Payment(
    val paid: Boolean?,
    val paymentMethod: String?,
    val paymentMethodTitle: String?,
    val transactionId: String?,
    val date: LocalDate?,
) : JsonSerializable {
    companion object : JsonSerializer<Payment> {
        override fun fromJSON(json: JSONObject, vararg args: Any?): Payment = Payment(
            json.getBooleanOrNull("paid"),
            json.getStringOrNull("payment_method")?.takeIf { it.isNotEmpty() },
            json.getStringOrNull("payment_method_title")?.takeIf { it.isNotEmpty() },
            json.getStringOrNull("transaction_id")?.takeIf { it.isNotEmpty() },
            json.getDateGmtOrNull("date_paid_gmt")?.toLocalDate(),
        )
    }

    override fun toJSON(): JSONObject = JSONObject()
        .put("paid", paid)
        .put("payment_method", paymentMethod)
        .put("payment_method_title", paymentMethodTitle)
        .put("transaction_id", transactionId)
        .putDateGmt("date_paid_gmt", date?.atStartOfDay())

    /** `true` if any of the fields is not null. date is not considered */
    val any: Boolean = paid == true ||
            listOf(paid, paymentMethod, paymentMethodTitle, transactionId)
                .any { it != null }
}
