package com.arnyminerz.filmagentaproto.database.remote

import android.util.Base64
import com.arnyminerz.filamagenta.core.database.data.woo.AvailablePayment
import com.arnyminerz.filamagenta.core.database.data.woo.Customer
import com.arnyminerz.filamagenta.core.database.data.woo.Event
import com.arnyminerz.filamagenta.core.database.data.woo.Order
import com.arnyminerz.filamagenta.core.remote.RemoteCommerceProto
import com.arnyminerz.filmagentaproto.BuildConfig

object RemoteCommerce : RemoteCommerceProto<
        Order,
        Order.Companion,
        Customer,
        Customer.Companion,
        Event,
        Event.Companion,
        AvailablePayment,
        AvailablePayment.Companion
        >(Order.Companion, Customer.Companion, Event.Companion, AvailablePayment.Companion) {
    override val host: String = BuildConfig.HOST

    override val wooConsumerKey: String = BuildConfig.WOO_CONSUMER_KEY

    override val wooConsumerSecret: String = BuildConfig.WOO_CONSUMER_SECRET

    override fun base64Encode(input: ByteArray): ByteArray = Base64.encode(input, Base64.NO_WRAP)

}
