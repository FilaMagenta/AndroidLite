package com.arnyminerz.filamagenta.core.utils.uri

import java.net.URI

fun URI.buildUpon(): UriBuilder =
    UriBuilder(
        scheme,
        host,
        rawPath.split('/').filter { it.isNotBlank() },
        query?.split('&')
            ?.associate { pair ->
                pair.split('=').let { it[0] to it.subList(1, it.size).joinToString("=") }
            } ?: emptyMap()
    )
