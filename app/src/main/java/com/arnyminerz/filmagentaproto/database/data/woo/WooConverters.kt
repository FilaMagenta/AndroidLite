package com.arnyminerz.filmagentaproto.database.data.woo

import androidx.room.TypeConverter
import com.arnyminerz.filamagenta.core.database.data.woo.CustomerProto
import com.arnyminerz.filamagenta.core.database.data.woo.EventProto
import com.arnyminerz.filamagenta.core.database.data.woo.OrderProto
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
    fun fromAttributeList(value: List<EventProto.Attribute>?): String? = value?.toJSON()?.toString()

    @TypeConverter
    fun toAttributeList(value: String?): List<EventProto.Attribute>? =
        value?.let { JSONArray(it).mapObjects { obj -> EventProto.Attribute.fromJSON(obj) } }


    @TypeConverter
    fun fromProductList(value: List<OrderProto.Product>?): String? = value?.toJSON()?.toString()

    @TypeConverter
    fun toProductList(value: String?): List<OrderProto.Product>? =
        value?.let { JSONArray(it).mapObjects { obj -> OrderProto.Product.fromJSON(obj) } }


    @TypeConverter
    fun fromDeliveryInformation(value: CustomerProto.DeliveryInformation?): String? =
        value?.toJSON()?.toString()

    @TypeConverter
    fun toDeliveryInformation(value: String?): CustomerProto.DeliveryInformation? =
        value?.let { CustomerProto.DeliveryInformation.fromJSON(JSONObject(it)) }


    @TypeConverter
    fun fromOrderPayment(value: OrderProto.Payment?): String? =
        value?.toJSON()?.toString()

    @TypeConverter
    fun toOrderPayment(value: String?): OrderProto.Payment? =
        value?.let { OrderProto.Payment.fromJSON(JSONObject(it)) }
}