package com.arnyminerz.filamagenta.core.utils

import java.util.Locale

val String.trimmedAndCaps: String
    get() = trim().capitalized()

fun String.capitalized(locale: Locale = Locale.getDefault()) =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }

fun String.capitalizedWords(locale: Locale = Locale.getDefault()) =
    split(" ").joinToString(" ") { it.capitalized(locale) }

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
