package com.arnyminerz.filmagentaproto.utils

import android.net.Uri
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import java.net.URL

val String.trimmedAndCaps: String
    get() = trim().capitalize(Locale.current)

fun String.capitalized(locale: java.util.Locale = java.util.Locale.getDefault()) =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }

fun Uri.toURL(): URL = URL(toString())
