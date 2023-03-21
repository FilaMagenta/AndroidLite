package com.arnyminerz.filmagentaproto.utils

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.browser.customtabs.CustomTabsIntent

@IntDef(Toast.LENGTH_SHORT, Toast.LENGTH_LONG)
annotation class ToastDuration

/**
 * Shows a toast with the given text and duration.
 * @param textRes The resource string id of the text to display.
 * @param duration The duration of the toast. Must be one of:
 * - [Toast.LENGTH_SHORT]
 * - [Toast.LENGTH_LONG]
 * @return The created toast.
 */
@UiThread
fun Context.toast(@StringRes textRes: Int, @ToastDuration duration: Int = Toast.LENGTH_SHORT): Toast =
    Toast.makeText(this, textRes, duration).also { it.show() }

/**
 * Shows a toast with the given text and duration.
 * @param text The text to display.
 * @param duration The duration of the toast. Must be one of:
 * - [Toast.LENGTH_SHORT]
 * - [Toast.LENGTH_LONG]
 * @return The created toast.
 */
@UiThread
fun Context.toast(text: String, @ToastDuration duration: Int = Toast.LENGTH_SHORT): Toast =
    Toast.makeText(this, text, duration).also { it.show() }

/**
 * Runs the [toast] method in the UI thread.
 * @see toast
 */
suspend fun Context.toastAsync(@StringRes textRes: Int, @ToastDuration duration: Int = Toast.LENGTH_SHORT) =
    ui { toast(textRes, duration) }

@UiThread
fun Context.launchUrl(url: String): CustomTabsIntent =
    CustomTabsIntent.Builder()
        .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
        .build()
        .also { it.launchUrl(this, Uri.parse(url)) }
