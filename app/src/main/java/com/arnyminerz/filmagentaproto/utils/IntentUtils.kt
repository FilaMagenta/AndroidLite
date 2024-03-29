package com.arnyminerz.filmagentaproto.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.CalendarContract
import android.provider.CalendarContract.Events
import androidx.activity.result.ActivityResultLauncher
import com.arnyminerz.filmagentaproto.R
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.Date
import kotlin.reflect.KClass

fun Context.launchCalendarInsert(begin: LocalDateTime, end: LocalDateTime? = null) {
    val intent = Intent(Intent.ACTION_INSERT).apply {
        setDataAndType(Events.CONTENT_URI, "vnd.android.cursor.item/event")
        putExtra(Events.TITLE, getString(R.string.calendar_trebuchet_title))
        putExtra(Events.DESCRIPTION, getString(R.string.calendar_trebuchet_description))
        val beginEpoch = begin.atZone(ZoneId.systemDefault()).toEpochSecond()
        val endEpoch = end?.atZone(ZoneId.systemDefault())?.toEpochSecond()
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginEpoch)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endEpoch ?: beginEpoch)
        putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
    }
    startActivity(intent)
}

fun Context.launchCalendarInsert(begin: LocalDateTime, end: LocalDateTime? = null, title: String, description: String?) {
    val intent = Intent(Intent.ACTION_INSERT).apply {
        setDataAndType(Events.CONTENT_URI, "vnd.android.cursor.item/event")
        putExtra(Events.TITLE, title)
        putExtra(Events.DESCRIPTION, description)
        val beginEpoch = begin.atZone(ZoneId.systemDefault()).toEpochSecond()
        val endEpoch = end?.atZone(ZoneId.systemDefault())?.toEpochSecond()
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginEpoch)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endEpoch ?: beginEpoch)
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

/** Launches the activity with `null` as input. */
fun ActivityResultLauncher<Void?>.launch() = launch(null)
