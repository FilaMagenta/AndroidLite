package com.arnyminerz.filamagenta.core.utils.uri

import java.net.URI

fun URI.buildUpon(): UriBuilder = UriBuilder(scheme, host, rawPath.split('/').filter { it.isNotBlank() })
