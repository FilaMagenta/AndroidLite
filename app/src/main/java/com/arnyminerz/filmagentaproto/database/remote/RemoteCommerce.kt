package com.arnyminerz.filmagentaproto.database.remote

import android.util.Base64
import com.arnyminerz.filamagenta.core.remote.RemoteCommerceProto
import com.arnyminerz.filmagentaproto.BuildConfig

object RemoteCommerce : RemoteCommerceProto() {
    override val host: String = BuildConfig.HOST

    override val wooConsumerKey: String = BuildConfig.WOO_CONSUMER_KEY

    override val wooConsumerSecret: String = BuildConfig.WOO_CONSUMER_SECRET

    override fun base64Encode(input: ByteArray): String = Base64.encodeToString(input, Base64.NO_WRAP)

}
