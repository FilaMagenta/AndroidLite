package com.arnyminerz.filamagenta.desktop.remote

import com.arnyminerz.filamagenta.core.remote.RemoteCommerceProto
import com.arnyminerz.filamagenta.core.resources.LocalCredentialsProvider
import java.util.Base64

object RemoteCommerce: RemoteCommerceProto() {
    override val host: String = LocalCredentialsProvider["host"]!!
    override val wooConsumerKey: String = LocalCredentialsProvider["woo.consumer_key"]!!
    override val wooConsumerSecret: String = LocalCredentialsProvider["woo.consumer_secret"]!!

    override fun base64Encode(input: ByteArray): String =
        Base64.getUrlEncoder().encodeToString(input)
}
