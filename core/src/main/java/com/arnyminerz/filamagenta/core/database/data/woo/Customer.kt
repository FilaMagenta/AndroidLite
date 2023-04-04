package com.arnyminerz.filamagenta.core.database.data.woo

import androidx.annotation.StringDef
import androidx.room.Entity
import com.arnyminerz.filamagenta.core.database.data.woo.customer.DeliveryInformation
import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer
import com.arnyminerz.filamagenta.core.utils.capitalizedWords
import com.arnyminerz.filamagenta.core.utils.getDateGmt
import com.arnyminerz.filamagenta.core.utils.putDateGmt
import java.util.Date
import org.json.JSONObject

const val ROLE_ADMINISTRATOR = "administrator"
const val ROLE_EDITOR = "editor"
const val ROLE_AUTHOR = "author"
const val ROLE_CONTRIBUTOR = "contributor"
const val ROLE_SUBSCRIBER = "subscriber"
const val ROLE_CUSTOMER = "customer"
const val ROLE_SHOP_MANAGER = "shop_manager"

@StringDef(
    ROLE_ADMINISTRATOR, ROLE_EDITOR, ROLE_AUTHOR, ROLE_CONTRIBUTOR, ROLE_SUBSCRIBER,
    ROLE_CUSTOMER, ROLE_SHOP_MANAGER
)
annotation class CustomerRole

@Entity(tableName = "customers", primaryKeys = ["id"])
class Customer(
    override val id: Long,
    val dateCreated: Date,
    val dateModified: Date,
    val email: String,
    val firstName: String,
    val lastName: String,
    @CustomerRole val role: String,
    val username: String,
    val billing: DeliveryInformation,
    val shipping: DeliveryInformation,
    val isPayingCustomer: Boolean,
    val avatarUrl: String,
) : WooClass(id) {
    companion object : JsonSerializer<Customer> {
        override fun fromJSON(json: JSONObject, vararg args: Any?): Customer = Customer(
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

    override fun toJSON(): JSONObject = JSONObject().apply {
        put("id", id)
        putDateGmt("date_created_gmt", dateCreated)
        putDateGmt("date_modified_gmt", dateModified)
        put("email", email)
        put("first_name", firstName)
        put("last_name", lastName)
        put("role", role)
        put("username", username)
        put("billing", billing.toJSON())
        put("shipping", shipping.toJSON())
        put("is_paying_customer", isPayingCustomer)
        put("avatar_url", avatarUrl)
    }

    val fullName: String by lazy { "$firstName $lastName".lowercase().capitalizedWords() }
}
