package com.arnyminerz.filamagenta.desktop.remote

import com.arnyminerz.filamagenta.core.remote.RemoteAuthenticationProto
import java.util.Base64

object RemoteAuthentication: RemoteAuthenticationProto(
    RemoteDatabaseInterface
) {
    override fun base64Encode(input: ByteArray): String = Base64.getMimeEncoder().encodeToString(input)

    override fun base64Decode(input: String): ByteArray = Base64.getMimeDecoder().decode(input)
}
