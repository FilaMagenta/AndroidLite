package com.arnyminerz.filmagentaproto.utils

import android.database.MatrixCursor
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CursorUtilsTest {
    private lateinit var cursor: MatrixCursor

    @Before
    fun insertData() {
        val columns = arrayOf("column1", "column2")
        cursor = MatrixCursor(columns)
        cursor.addRow(arrayOf("1", "value"))

        cursor.moveToNext()
    }

    @After
    fun closeCursor() {
        cursor.close()
    }

    @Test
    fun test_getString_columnName() {
        val str = cursor.getString("column2")
        assertEquals("value", str)
    }

    @Test
    fun test_getInt_columnName() {
        val int = cursor.getInt("column1")
        assertEquals(1, int)
    }
}