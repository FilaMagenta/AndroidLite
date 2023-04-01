package com.arnyminerz.filamagenta.core.database.prototype

import java.sql.ResultSet

interface RemoteDataParser <T: Any> {
    fun parse(row: ResultSet): T
}
