package com.arnyminerz.filmagentaproto.implementation

import com.arnyminerz.filamagenta.core.database.data.woo.Event
import com.arnyminerz.filamagenta.core.database.data.woo.StockStatus
import com.arnyminerz.filamagenta.core.database.data.woo.event.EventType
import com.arnyminerz.filamagenta.core.utils.currentDate
import com.arnyminerz.filamagenta.core.utils.currentDateTime
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class TestEvent {
    private val fixedDateTime = LocalDateTime.of(2023, 1, 1, 0, 0, 0)
    private val fixedDate = LocalDate.of(2023, 1, 1)

    @Before
    fun mock_currentDate_currentDateTime() {
        mockkStatic("com.arnyminerz.filamagenta.core.utils.DateUtilsKt")

        // Mock the currentDate() function to return specific time
        every { currentDate() } returns fixedDate
        assertEquals(fixedDate, currentDate())
        verify { currentDate() }

        // Mock the currentDateTime() function to return specific time
        every { currentDateTime() } returns fixedDateTime
        assertEquals(fixedDateTime, currentDateTime())
        verify { currentDateTime() }
    }

    private val datesEvent = Event(
        id = 1,
        name = "14 SAN JORGE, cena",
        slug = "san-jorge-cena",
        permalink = "",
        dateCreated = LocalDate.of(2023, 1, 1),
        dateModified = LocalDate.of(2023, 1, 1),
        description = "",
        shortDescription = "",
        price = 0.0,
        attributes = emptyList(),
        stockStatus = StockStatus.InStock,
        stockQuantity = 128,
    )

    @Test
    fun test_event() {
        val event = datesEvent.copy(
            shortDescription = "Comida organizada por mesas. No se estregan tickets.\nIndicar el responsable de mesa.",
        )
        assertEquals(EventType.Dinner, event.type)
        assertEquals("SAN JORGE, cena", event.title)
        assertEquals(14, event.index)
        assertEquals(
            "Comida organizada por mesas. No se estregan tickets.\nIndicar el responsable de mesa.",
            event.cutDescription,
        )
    }

    @Test
    fun test_lunchNoDate() {
        val event = datesEvent.copy(
            shortDescription = "RESERVAS hasta el domingo 2 de abril.\nComida organizada por mesas. No se estregan tickets.\nIndicar el responsable de mesa.",
        )
        assertEquals(fixedDateTime, currentDateTime())
        assertFalse(event.hasPassed)
        assertEquals(LocalDateTime.of(2023, 4, 2, 23, 59), event.acceptsReservationsUntil)
        assertNull(event.eventDate)
        assertEquals(
            "Comida organizada por mesas. No se estregan tickets.\nIndicar el responsable de mesa.",
            event.cutDescription,
        )
    }

    @Test
    fun test_lunchNoLimit() {
        val event = datesEvent.copy(
            shortDescription = "Domingo 2 de abril.\nComida organizada por mesas. No se estregan tickets.\nIndicar el responsable de mesa.",
        )
        assertEquals(LocalDateTime.of(2023, 4, 2, 0, 0), event.eventDate)
        assertNull(event.acceptsReservationsUntil)
        assertFalse(event.hasPassed)
        assertEquals(
            "Comida organizada por mesas. No se estregan tickets.\nIndicar el responsable de mesa.",
            event.cutDescription,
        )
    }

    @Test
    fun test_lunchDateAndLimit() {
        val event = datesEvent.copy(
            shortDescription = "RESERVAS hasta el domingo 2 de abril.\nDía 25 de abril.\nComida organizada por mesas. No se estregan tickets.\nIndicar el responsable de mesa.",
        )
        assertEquals(LocalDateTime.of(2023, 4, 25, 0, 0), event.eventDate)
        assertEquals(LocalDateTime.of(2023, 4, 2, 23, 59), event.acceptsReservationsUntil)
        assertFalse(event.hasPassed)
        assertEquals(
            "Comida organizada por mesas. No se estregan tickets.\nIndicar el responsable de mesa.",
            event.cutDescription,
        )
    }
}