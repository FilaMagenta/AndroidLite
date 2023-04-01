package com.arnyminerz.filmagentaproto.processing

import com.arnyminerz.filmagentaproto.database.remote.RemoteCommerce
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkObject
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TestOrdersFetching {
    private val customerId = 1L

    private val endpointOrders = RemoteCommerce.ordersEndpoint.buildUpon()
        .appendQueryParameter("customer", customerId.toString())
        .appendQueryParameter("page", "1")
        .appendQueryParameter("per_page", "100")
        .build()

    private fun getResourceSample(filename: String): String =
        this::class.java.classLoader?.getResourceAsStream("samples/$filename")!!
            .use { stream -> stream.bufferedReader().readText() }

    @Before
    fun mock_endpoint_orders() {
        mockkObject(RemoteCommerce)
        val orderContents = getResourceSample("customer-orders.json")
        coEvery { RemoteCommerce.get(endpointOrders) } returns orderContents
    }

    @Test
    fun test_endpoint_orders() = runBlocking {
        val orders = RemoteCommerce.orderList(customerId)
        assertEquals(2, orders.size)

        val items = orders.flatMap { it.items }
        assertEquals(13, items.size)
    }

    @After
    fun verify_endpoint_orders() {
        coVerify { RemoteCommerce.get(endpointOrders) }
    }
}
