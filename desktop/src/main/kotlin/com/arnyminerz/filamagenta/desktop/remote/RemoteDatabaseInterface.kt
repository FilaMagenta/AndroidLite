package com.arnyminerz.filamagenta.desktop.remote

import com.arnyminerz.filamagenta.core.remote.RemoteDatabaseInterfaceProto
import com.arnyminerz.filamagenta.core.resources.LocalCredentialsProvider

object RemoteDatabaseInterface: RemoteDatabaseInterfaceProto() {
    override val dbHostname: String = LocalCredentialsProvider["db.hostname"]!!
    override val dbPort: Int = LocalCredentialsProvider["db.port"]!!.toInt()
    override val dbDatabase: String = LocalCredentialsProvider["db.database"]!!
    override val dbUsername: String = LocalCredentialsProvider["db.username"]!!
    override val dbPassword: String = LocalCredentialsProvider["db.password"]!!

}