package com.arnyminerz.filamagenta.desktop.remote

import com.arnyminerz.filamagenta.core.remote.RemoteDatabaseInterfaceProto
import com.arnyminerz.filamagenta.core.resources.LocalCredentialsProvider

object RemoteDatabaseInterface: RemoteDatabaseInterfaceProto() {
    override val dbHostname: String = LocalCredentialsProvider.get("db.hostname")!!
    override val dbPort: Int = LocalCredentialsProvider.get("db.port")!!.toInt()
    override val dbDatabase: String = LocalCredentialsProvider.get("db.database")!!
    override val dbUsername: String = LocalCredentialsProvider.get("db.username")!!
    override val dbPassword: String = LocalCredentialsProvider.get("db.password")!!

}