package com.arnyminerz.filamagenta.core.utils.uri

import java.net.URI

data class UriBuilder(
    private val protocol: String = "https",
    private val host: String = "",
    private val path: List<String> = emptyList(),
    private val query: Map<String, String> = emptyMap(),
) {

    fun appendPath(vararg pieces: String) = copy(
        path = path.toMutableList().apply { addAll(pieces.flatMap { it.split('/') }) }
    )

    fun appendQueryParameter(key: String, value: String) = copy(
        query = query.toMutableMap().apply { put(key, value) }
    )

    fun build(): URI = URI.create(
        "$protocol://$host/${path.joinToString("/")}" +
                query.takeIf { it.isNotEmpty() }
                    ?.let { queryElements ->
                        "?" + queryElements
                            .toList()
                            .joinToString("&") { (key, value) -> "$key=$value" }
                    }
    )

}
