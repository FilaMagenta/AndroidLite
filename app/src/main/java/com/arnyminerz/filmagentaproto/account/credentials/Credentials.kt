package com.arnyminerz.filmagentaproto.account.credentials

/**
 * Stores the credentials required for accessing the user's personal dashboard.
 */
data class Credentials(
    val nif: String,
    val password: String,
    val token: String,
)
