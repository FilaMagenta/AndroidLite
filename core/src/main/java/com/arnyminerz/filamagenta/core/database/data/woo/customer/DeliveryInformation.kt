package com.arnyminerz.filamagenta.core.database.data.woo.customer

import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializable
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer
import com.arnyminerz.filamagenta.core.utils.getStringOrNull
import org.json.JSONObject

data class DeliveryInformation(
    val firstName: String?,
    val lastName: String?,
    val company: String?,
    val address1: String?,
    val address2: String?,
    val city: String?,
    val postcode: String?,
    val country: String?,
    val state: String?,
    val email: String?,
    val phone: String?,
) : JsonSerializable {
    companion object : JsonSerializer<DeliveryInformation> {
        override fun fromJSON(json: JSONObject, vararg args: Any?): DeliveryInformation = DeliveryInformation(
            json.getStringOrNull("first_name"),
            json.getStringOrNull("last_name"),
            json.getStringOrNull("company"),
            json.getStringOrNull("address_1"),
            json.getStringOrNull("address_2"),
            json.getStringOrNull("city"),
            json.getStringOrNull("postcode"),
            json.getStringOrNull("country"),
            json.getStringOrNull("state"),
            json.getStringOrNull("email"),
            json.getStringOrNull("phone"),
        )
    }

    override fun toJSON(): JSONObject = JSONObject().apply {
        put("first_name", firstName)
        put("last_name", lastName)
        put("company", company)
        put("address_1", address1)
        put("address_2", address2)
        put("city", city)
        put("postcode", postcode)
        put("country", country)
        put("state", state)
        put("email", email)
        put("phone", phone)
    }
}
