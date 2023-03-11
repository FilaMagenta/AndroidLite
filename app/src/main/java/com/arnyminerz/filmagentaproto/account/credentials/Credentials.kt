package com.arnyminerz.filmagentaproto.account.credentials

/**
 * Stores the credentials required for accessing the user's personal dashboard.
 */
data class Credentials(
    val name: String,
    val nif: String,
    val token: String,
)
