package com.arnyminerz.filamagenta.core.utils.uri

import java.net.URI

data class UriBuilder(
    val protocol: String = "https",
    val host: String = "",
    val path: List<String> = emptyList(),
    val query: Map<String, String> = emptyMap(),
) {

    fun appendPath(vararg pieces: String) = copy(
        path = path.toMutableList().apply {
            addAll(pieces.flatMap { it.split('/') })
        }
    )

    fun appendQueryParameter(key: String, value: String) = copy(
        query = query.toMutableMap().apply { put(key, value) }
    )

    fun build(): URI {
        val base = "$protocol://$host"
        val pathPieces = path.joinToString("/")
        val queryString = query
            .takeIf { it.isNotEmpty() }
            ?.let { queryElements ->
                "?" + queryElements
                    .toList()
                    .joinToString("&") { (key, value) -> "$key=$value" }
            } ?: ""
        return URI.create("$base/$pathPieces$queryString")
    }

}
