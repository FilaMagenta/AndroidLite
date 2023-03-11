package com.arnyminerz.filmagentaproto.account.credentials

import android.accounts.Account
import android.accounts.AccountManager
import com.arnyminerz.filmagentaproto.database.prototype.JsonSerializable
import com.arnyminerz.filmagentaproto.database.prototype.JsonSerializer
import org.json.JSONObject

/**
 * Stores the credentials required for accessing the WooCommerce REST API.
 */
data class WCCredentials(
    val keyId: Int,
    val consumerKey: String,
    val consumerSecret: String,
): JsonSerializable {
    companion object: JsonSerializer<WCCredentials> {
        override fun fromJSON(json: JSONObject): WCCredentials = WCCredentials(
            json.getInt("key_id"),
            json.getString("consumer_key"),
            json.getString("consumer_secret"),
        )

        fun fromAccount(accountManager: AccountManager, account: Account): WCCredentials? {
            val keyId: Int? = accountManager.getUserData(account, "key_id")?.toIntOrNull()
            val key: String? = accountManager.getUserData(account, "consumer_key")
            val secret: String? = accountManager.getUserData(account, "consumer_secret")
            return if (keyId != null && key != null && secret != null)
                WCCredentials(keyId, key, secret)
            else
                null
        }
    }

    override fun toJSON(): JSONObject = JSONObject().apply {
        put("key_id", keyId)
        put("consumer_key", consumerKey)
        put("consumer_secret", consumerSecret)
    }
}
