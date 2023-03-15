package com.arnyminerz.filmagentaproto.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.CalendarContract
import android.provider.CalendarContract.Events
import com.arnyminerz.filmagentaproto.R
import java.util.Date
import kotlin.reflect.KClass

fun Context.launchCalendarInsert(begin: Date, end: Date? = null) {
    val intent = Intent(Intent.ACTION_INSERT).apply {
        setDataAndType(Events.CONTENT_URI, "vnd.android.cursor.item/event")
        putExtra(Events.TITLE, getString(R.string.calendar_trebuchet_title))
        putExtra(Events.DESCRIPTION, getString(R.string.calendar_trebuchet_description))
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, begin.time)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end?.time ?: begin.time)
        putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
    }
    startActivity(intent)
}

fun Context.launchCalendarInsert(begin: Date, end: Date? = null, title: String, description: String?) {
    val intent = Intent(Intent.ACTION_INSERT).apply {
        setDataAndType(Events.CONTENT_URI, "vnd.android.cursor.item/event")
        putExtra(Events.TITLE, title)
        putExtra(Events.DESCRIPTION, description)
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, begin.time)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end?.time ?: begin.time)
        putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
    }
    startActivity(intent)
}

fun <T: Any> Intent.getParcelableExtraCompat(key: String, kClass: KClass<T>) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(key, kClass.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(key)
    }
