package com.arnyminerz.filmagentaproto.database.prototype

import org.json.JSONObject

interface JsonSerializable {
    fun toJSON(): JSONObject
}