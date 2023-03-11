package com.arnyminerz.filmagentaproto.utils

import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale

val String.trimmedAndCaps: String
    get() = trim().capitalize(Locale.current)

fun String.capitalized(locale: java.util.Locale = java.util.Locale.getDefault()) =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
