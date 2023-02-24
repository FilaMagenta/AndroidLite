package com.arnyminerz.filmagentaproto.utils

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.provider.CalendarContract.Events
import com.arnyminerz.filmagentaproto.R
import java.util.Date

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
