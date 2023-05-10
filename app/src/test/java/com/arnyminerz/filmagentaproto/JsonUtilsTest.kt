package com.arnyminerz.filmagentaproto

import com.arnyminerz.filamagenta.core.utils.getDateGmt
import com.arnyminerz.filamagenta.core.utils.getDateGmtOrNull
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Month

class JsonUtilsTest {
    private val obj = JSONObject().apply {
        put("date", "2023-10-12T10:00:00")
    }

    @Test
    fun test_getDateGmt() {
        val date = obj.getDateGmt("date")
        assertEquals("GMT", date.zone.id)
        assertEquals(2023, date.year)
        assertEquals(Month.OCTOBER, date.month)
        assertEquals(12, date.dayOfMonth)
        assertEquals(10, date.hour)
        assertEquals(0, date.minute)
        assertEquals(0, date.second)
    }

    @Test
    fun test_getDateGmtOrNull() {
        assertNull(obj.getDateGmtOrNull("invalid"))
        assertNotNull(obj.getDateGmtOrNull("date"))
    }
}
