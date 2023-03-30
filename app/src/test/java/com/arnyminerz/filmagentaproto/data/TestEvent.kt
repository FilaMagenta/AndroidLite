package com.arnyminerz.filmagentaproto.data

import com.arnyminerz.filmagentaproto.database.data.woo.Event
import com.arnyminerz.filmagentaproto.database.data.woo.StockStatus
import com.arnyminerz.filmagentaproto.utils.now
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.verify
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class TestEvent {
    @Before
    fun mock_now() {
        // Mock the now() function to return specific time
        mockkStatic(::now)
        every { now() } returns Date(1680176818000)
        assertEquals(1680176818000, now().time)
        verify { now() }
    }

    @Test
    fun test_lunchImplicit() {
        val event = Event(
            id = 1,
            name = "14 SAN JORGE, cena",
            slug = "san-jorge-cena",
            permalink = "",
            dateCreated = Date(1680176818000),
            dateModified = Date(1680176818000),
            description = "",
            shortDescription = "RESERVAS hasta el domingo 2 de abril.\nComida organizada por mesas. No se estregan tickets.\nIndicar el responsable de mesa.\nEn DETALLES DE FACTURACION, abajo pone INFORAMCION ADICIONAL, anotar el responsable de mesa.",
            price = 0.0,
            attributes = emptyList(),
            stockStatus = StockStatus.InStock,
            stockQuantity = 128,
        )
        assertEquals(Event.Type.Dinner, event.type)
        assertEquals("SAN JORGE, cena", event.title)
        assertEquals(14, event.index)
        assertFalse(event.hasPassed)
        assertEquals(Date(1680472740000), event.acceptsReservationsUntil)
        assertNull(event.eventDate)
    }
}