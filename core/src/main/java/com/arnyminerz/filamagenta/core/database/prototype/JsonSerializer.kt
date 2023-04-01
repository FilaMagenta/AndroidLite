package com.arnyminerz.filamagenta.core.database.prototype

import org.json.JSONObject

interface JsonSerializer <T: Any> {
    fun fromJSON(json: JSONObject): T
}
