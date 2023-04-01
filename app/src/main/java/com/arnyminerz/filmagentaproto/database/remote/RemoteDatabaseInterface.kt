package com.arnyminerz.filmagentaproto.database.remote

import com.arnyminerz.filamagenta.core.remote.RemoteDatabaseInterfaceProto
import com.arnyminerz.filmagentaproto.BuildConfig

object RemoteDatabaseInterface: RemoteDatabaseInterfaceProto() {
    override val dbHostname: String = BuildConfig.DB_HOSTNAME
    override val dbPort: Int = BuildConfig.DB_PORT
    override val dbDatabase: String = BuildConfig.DB_DATABASE
    override val dbUsername: String = BuildConfig.DB_USERNAME
    override val dbPassword: String = BuildConfig.DB_PASSWORD
}
