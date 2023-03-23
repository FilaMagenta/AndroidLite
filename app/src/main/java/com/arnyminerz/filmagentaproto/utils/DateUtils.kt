package com.arnyminerz.filmagentaproto.utils

import java.util.Calendar
import java.util.Date

fun now(): Date {
    val calendar = Calendar.getInstance()
    return calendar.time
}
