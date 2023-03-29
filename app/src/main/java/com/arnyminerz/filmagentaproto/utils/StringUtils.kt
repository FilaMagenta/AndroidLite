package com.arnyminerz.filmagentaproto.utils

import android.net.Uri
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import java.net.URL

val String.trimmedAndCaps: String
    get() = trim().capitalize(Locale.current)

fun String.capitalized(locale: java.util.Locale = java.util.Locale.getDefault()) =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }

fun String.capitalizedWords(locale: java.util.Locale = java.util.Locale.getDefault()) =
    split(" ").joinToString(" ") { it.capitalized(locale) }

fun Uri.toURL(): URL = URL(toString())

/**
 * Returns true if the word is all capital letters.
 */
val String.allUpperCase: Boolean
    get() = all { it.isUpperCase() }

/**
 * Returns true if the word is all lower-case letters.
 */
val String.allLowerCase: Boolean
    get() = all { it.isLowerCase() }

/**
 * Returns true if the word is all numbers.
 */
val String.isNumber: Boolean
    get() = all { it.isDigit() }
