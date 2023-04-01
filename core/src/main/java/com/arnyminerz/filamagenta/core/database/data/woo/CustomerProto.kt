package com.arnyminerz.filamagenta.core.database.data.woo

import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializable
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer
import com.arnyminerz.filamagenta.core.utils.capitalizedWords
import com.arnyminerz.filamagenta.core.utils.getDateGmt
import com.arnyminerz.filamagenta.core.utils.getStringOrNull
import java.util.Date
import org.json.JSONObject

const val ROLE_ADMINISTRATOR = "administrator"
const val ROLE_EDITOR = "editor"
const val ROLE_AUTHOR = "author"
const val ROLE_CONTRIBUTOR = "contributor"
const val ROLE_SUBSCRIBER = "subscriber"
const val ROLE_CUSTOMER = "customer"
const val ROLE_SHOP_MANAGER = "shop_manager"

open class CustomerProto(
    override val id: Long,
    open val dateCreated: Date,
    open val dateModified: Date,
    open val email: String,
    open val firstName: String,
    open val lastName: String,
    open val role: String,
    open val username: String,
    open val billing: DeliveryInformation,
    open val shipping: DeliveryInformation,
    open val isPayingCustomer: Boolean,
    open val avatarUrl: String,
) : WooClass(id) {
    companion object : JsonSerializer<CustomerProto> {
        override fun fromJSON(json: JSONObject): CustomerProto = CustomerProto(
            json.getLong("id"),
            json.getDateGmt("date_created_gmt"),
            json.getDateGmt("date_modified_gmt"),
            json.getString("email"),
            json.getString("first_name"),
            json.getString("last_name"),
            json.getString("role"),
            json.getString("username"),
            json.getJSONObject("billing").let { DeliveryInformation.fromJSON(it) },
            json.getJSONObject("shipping").let { DeliveryInformation.fromJSON(it) },
            json.getBoolean("is_paying_customer"),
            json.getString("avatar_url"),
        )
    }

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
            override fun fromJSON(json: JSONObject): DeliveryInformation = DeliveryInformation(
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

    val fullName: String by lazy { "$firstName $lastName".lowercase().capitalizedWords() }
}
