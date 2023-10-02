package com.arnyminerz.filamagenta.core.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Date

@Deprecated("Use Java 8", replaceWith = ReplaceWith("currentDateTime()"))
fun now(): Date {
    val calendar = Calendar.getInstance()
    return calendar.time
}

fun currentDate(): LocalDate = LocalDate.now()

fun currentDateTime(): LocalDateTime = LocalDateTime.now()

fun java.sql.Date.toJava8(): Instant = Instant.ofEpochMilli(time)
