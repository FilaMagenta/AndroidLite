package com.arnyminerz.filamagenta.core

import com.arnyminerz.filamagenta.core.utils.uri.UriBuilder
import com.arnyminerz.filamagenta.core.utils.uri.buildUpon
import java.net.URI
import org.junit.Assert.assertEquals
import org.junit.Test

class TestUriBuilder {
    @Test
    fun test_builder() {
        val uriBuilder = UriBuilder(host = "example.com")
            .appendPath("test")
            .appendQueryParameter("key", "value")
        assertEquals("https", uriBuilder.protocol)
        assertEquals("example.com", uriBuilder.host)
        assertEquals(listOf("test"), uriBuilder.path)
        assertEquals(mapOf("key" to "value"), uriBuilder.query)
        val uri = uriBuilder.build()
        assertEquals("https://example.com/test?key=value", uri.toString())
    }

    @Test
    fun test_buildUpon() {
        val srcUri = URI.create("https://example.com/test?key=value")
        val uriBuilder = srcUri.buildUpon()
        assertEquals("https", uriBuilder.protocol)
        assertEquals("example.com", uriBuilder.host)
        assertEquals(listOf("test"), uriBuilder.path)
        assertEquals(mapOf("key" to "value"), uriBuilder.query)
        val uri = uriBuilder.build()
        assertEquals("https://example.com/test?key=value", uri.toString())
    }

    @Test
    fun test_buildUpon_empty() {
        val srcUri = URI.create("https://example.com")
        val uriBuilder = srcUri.buildUpon()
        assertEquals(0, uriBuilder.path.size)
        assertEquals(0, uriBuilder.query.size)
    }

    @Test
    fun test_buildUpon_query() {
        val srcUri = URI.create("https://example.com/test?key=value&another==complex=")
        val uriBuilder = srcUri.buildUpon()
        assertEquals(mapOf("key" to "value", "another" to "=complex="), uriBuilder.query)
    }
}
