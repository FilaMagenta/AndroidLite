package com.arnyminerz.filamagenta.core.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

fun now(): Date {
    val calendar = Calendar.getInstance()
    return calendar.time
}

/**
 * Checks whether or not the given [input] is a valid date for `this` format.
 * @param input The date to check for. `null` will always return `false`.
 * @return `true` if the given [input] is a valid date, `false` otherwise.
 */
fun SimpleDateFormat.matches(input: String?) = input != null && try {
    parse(input)
    true
} catch (e: ParseException) {
    false
}
