package com.arnyminerz.filmagentaproto.account

import android.util.Base64
import com.arnyminerz.filamagenta.core.remote.RemoteAuthenticationProto
import com.arnyminerz.filmagentaproto.database.remote.RemoteDatabaseInterface

object RemoteAuthentication: RemoteAuthenticationProto(RemoteDatabaseInterface) {
    override fun base64Encode(input: ByteArray): String = Base64.encodeToString(input, Base64.NO_WRAP)

    override fun base64Decode(input: String): ByteArray = Base64.decode(input, Base64.NO_WRAP)
}