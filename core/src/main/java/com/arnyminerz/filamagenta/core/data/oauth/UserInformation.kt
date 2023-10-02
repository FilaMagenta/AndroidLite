package com.arnyminerz.filamagenta.core.data.oauth

import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer
import com.arnyminerz.filamagenta.core.utils.getStringJSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class UserInformation(
    val id: Int,
    val login: String,
    val niceName: String,
    val email: String,
    val registration: LocalDateTime,
    val status: Int,
    val displayName: String,
    val roles: List<String>
) {
    companion object : JsonSerializer<UserInformation> {
        override fun fromJSON(json: JSONObject, vararg args: Any?): UserInformation =
            UserInformation(
                json.getInt("ID"),
                json.getString("user_login"),
                json.getString("user_nicename"),
                json.getString("user_email"),
                json.getString("user_registered").let {
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    LocalDateTime.parse(it, formatter)
                },
                json.getInt("user_status"),
                json.getString("display_name"),
                json.getStringJSONArray("user_roles")
            )
    }
}
