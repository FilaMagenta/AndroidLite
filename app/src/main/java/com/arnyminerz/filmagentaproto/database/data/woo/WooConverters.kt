package com.arnyminerz.filmagentaproto.database.data.woo

import androidx.room.TypeConverter
import com.arnyminerz.filamagenta.core.database.data.woo.customer.DeliveryInformation
import com.arnyminerz.filamagenta.core.database.data.woo.event.Attribute
import com.arnyminerz.filamagenta.core.database.data.woo.order.Payment
import com.arnyminerz.filamagenta.core.database.data.woo.order.Product
import com.arnyminerz.filamagenta.core.utils.mapObjects
import com.arnyminerz.filamagenta.core.utils.toJSON
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
    fun fromAttributeList(value: List<Attribute>?): String? = value?.toJSON()?.toString()

    @TypeConverter
    fun toAttributeList(value: String?): List<Attribute>? =
        value?.let { JSONArray(it).mapObjects { obj -> Attribute.fromJSON(obj) } }


    @TypeConverter
    fun fromProductList(value: List<Product>?): String? = value?.toJSON()?.toString()

    @TypeConverter
    fun toProductList(value: String?): List<Product>? =
        value?.let { JSONArray(it).mapObjects { obj -> Product.fromJSON(obj) } }


    @TypeConverter
    fun fromDeliveryInformation(value: DeliveryInformation?): String? =
        value?.toJSON()?.toString()

    @TypeConverter
    fun toDeliveryInformation(value: String?): DeliveryInformation? =
        value?.let { DeliveryInformation.fromJSON(JSONObject(it)) }


    @TypeConverter
    fun fromOrderPayment(value: Payment?): String? =
        value?.toJSON()?.toString()

    @TypeConverter
    fun toOrderPayment(value: String?): Payment? =
        value?.let { Payment.fromJSON(JSONObject(it)) }
}