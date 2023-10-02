package com.arnyminerz.filamagenta.core.remote.result.auth

import com.arnyminerz.filamagenta.core.security.AccessToken

sealed class TokenResult {
    data class Success(val token: AccessToken): TokenResult()

    data class Failure(val code: Int, val message: String): TokenResult()
}
