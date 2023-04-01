package com.arnyminerz.filamagenta.core.database.data.woo

import com.arnyminerz.filamagenta.core.database.data.woo.customer.DeliveryInformation
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer
import com.arnyminerz.filamagenta.core.utils.capitalizedWords
import com.arnyminerz.filamagenta.core.utils.getDateGmt
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
        override fun fromJSON(json: JSONObject, vararg args: Any?): CustomerProto = CustomerProto(
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

    val fullName: String by lazy { "$firstName $lastName".lowercase().capitalizedWords() }
}
