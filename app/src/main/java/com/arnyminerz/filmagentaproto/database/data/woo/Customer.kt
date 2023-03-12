package com.arnyminerz.filmagentaproto.database.data.woo

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arnyminerz.filmagentaproto.database.prototype.JsonSerializable
import com.arnyminerz.filmagentaproto.database.prototype.JsonSerializer
import com.arnyminerz.filmagentaproto.utils.getDateGmt
import com.arnyminerz.filmagentaproto.utils.getIntOrNull
import com.arnyminerz.filmagentaproto.utils.getStringOrNull
import java.util.Date
import org.json.JSONObject

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey val id: Long,
    val dateCreated: Date,
    val dateModified: Date,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: String,
    val username: String,
    val billing: DeliveryInformation,
    val shipping: DeliveryInformation,
    val isPayingCustomer: Boolean,
    val avatarUrl: String,
) {
    companion object: JsonSerializer<Customer> {
        override fun fromJSON(json: JSONObject): Customer = Customer(
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
        val postcode: Int?,
        val country: String?,
        val state: String?,
        val email: String?,
        val phone: String?,
    ): JsonSerializable {
        companion object: JsonSerializer<DeliveryInformation> {
            override fun fromJSON(json: JSONObject): DeliveryInformation = DeliveryInformation(
                json.getStringOrNull("first_name"),
                json.getStringOrNull("last_name"),
                json.getStringOrNull("company"),
                json.getStringOrNull("address_1"),
                json.getStringOrNull("address_2"),
                json.getStringOrNull("city"),
                json.getIntOrNull("postcode"),
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
}
