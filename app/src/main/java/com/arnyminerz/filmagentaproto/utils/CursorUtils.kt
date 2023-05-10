package com.arnyminerz.filmagentaproto.utils

import android.database.Cursor

/**
 * Runs [Cursor.getString] with the index named after the given [columnName].
 * @return The value stored as string (may be null), or null if no column named [columnName].
 */
fun Cursor.getString(columnName: String): String? = getColumnIndex(columnName)
    .takeIf { it >= 0 }
    ?.let { getString(it) }

/**
 * Runs [Cursor.getInt] with the index named after the given [columnName].
 * @return The value stored as int, or null if no column named [columnName].
 */
fun Cursor.getInt(columnName: String): Int? = getColumnIndex(columnName)
    .takeIf { it >= 0 }
    ?.let { getInt(it) }
