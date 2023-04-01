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
        assertEquals("https", uriBuilder.protocol)
        assertEquals("example.com", uriBuilder.host)
        assertEquals(listOf("test"), uriBuilder.path)
        assertEquals(0, uriBuilder.query.size)
        val uri = uriBuilder.build()
        assertEquals("https://example.com/test", uri.toString())
    }

    @Test
    fun test_buildUpon() {
        val srcUri = URI.create("https://example.com/test")
        val uriBuilder = srcUri.buildUpon()
        assertEquals("https", uriBuilder.protocol)
        assertEquals("example.com", uriBuilder.host)
        assertEquals(listOf("test"), uriBuilder.path)
        assertEquals(0, uriBuilder.query.size)
        val uri = uriBuilder.build()
        assertEquals("https://example.com/test", uri.toString())
    }
}
