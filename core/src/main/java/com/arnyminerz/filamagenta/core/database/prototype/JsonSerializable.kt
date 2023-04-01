package com.arnyminerz.filamagenta.core.database.prototype

import org.json.JSONObject

interface JsonSerializable {
    fun toJSON(): JSONObject
}
