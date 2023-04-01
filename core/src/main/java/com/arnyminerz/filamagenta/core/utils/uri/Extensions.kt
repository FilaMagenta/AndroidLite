package com.arnyminerz.filamagenta.core.utils.uri

import java.net.URI

fun URI.buildUpon(): UriBuilder = UriBuilder(authority, host, path.split('/'))
