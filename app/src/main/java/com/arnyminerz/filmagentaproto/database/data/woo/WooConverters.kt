package com.arnyminerz.filmagentaproto.database.data.woo

import androidx.room.TypeConverter
import com.arnyminerz.filmagentaproto.utils.mapObjects
import com.arnyminerz.filmagentaproto.utils.toJSON
import java.util.Date
import org.json.JSONArray
import org.json.JSONObject

class WooConverters {

    @TypeConverter
    fun fromDate(value: Date?): Long? = value?.time

    @TypeConverter
    fun toDate(value: Long?): Date? = value?.let { Date(it) }


    @TypeConverter
    fun fromLongList(value: List<Long>?): String? = value?.let { JSONArray(it) }?.toString()

    @TypeConverter
    fun toLongList(value: String?): List<Long>? =
        value?.let { JSONArray(it).let { array -> (0 until array.length()).map { i -> array.getLong(i) } } }


    @TypeConverter
    fun fromAttributeList(value: List<Event.Attribute>?): String? = value?.toJSON()?.toString()

    @TypeConverter
    fun toAttributeList(value: String?): List<Event.Attribute>? =
        value?.let { JSONArray(it).mapObjects { obj -> Event.Attribute.fromJSON(obj) } }


    @TypeConverter
    fun fromProductList(value: List<Order.Product>?): String? = value?.toJSON()?.toString()

    @TypeConverter
    fun toProductList(value: String?): List<Order.Product>? =
        value?.let { JSONArray(it).mapObjects { obj -> Order.Product.fromJSON(obj) } }


    @TypeConverter
    fun fromDeliveryInformation(value: Customer.DeliveryInformation?): String? =
        value?.toJSON()?.toString()

    @TypeConverter
    fun toDeliveryInformation(value: String?): Customer.DeliveryInformation? =
        value?.let { Customer.DeliveryInformation.fromJSON(JSONObject(it)) }


    @TypeConverter
    fun fromOrderPayment(value: Order.Payment?): String? =
        value?.toJSON()?.toString()

    @TypeConverter
    fun toOrderPayment(value: String?): Order.Payment? =
        value?.let { Order.Payment.fromJSON(JSONObject(it)) }
}