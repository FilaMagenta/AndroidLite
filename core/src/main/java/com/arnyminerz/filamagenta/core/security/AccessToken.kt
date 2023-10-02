package com.arnyminerz.filamagenta.core.security

import com.arnyminerz.filamagenta.core.database.prototype.JsonSerializer
import org.json.JSONObject
import java.time.Instant

data class AccessToken(
    val token: String,
    val timestamp: Instant,
    val expiresIn: Int,
    val refreshToken: String
) {
    companion object: JsonSerializer<AccessToken> {
        override fun fromJSON(json: JSONObject, vararg args: Any?): AccessToken = AccessToken(
            json.getString("access_token"),
            Instant.now(),
            json.getInt("expires_in"),
            json.getString("refresh_token")
        )
    }

    val expiration: Instant get() = timestamp.plusSeconds(expiresIn.toLong())

    /**
     * Compares the current time with [timestamp] and [expiresIn] to check whether the token has
     * expired or not.
     */
    fun isExpired(): Boolean = Instant.now() > expiration
}
