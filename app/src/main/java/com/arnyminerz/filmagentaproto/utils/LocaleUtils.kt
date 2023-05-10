package com.arnyminerz.filmagentaproto.utils

import androidx.core.os.LocaleListCompat
import java.util.Locale

/** Converts the [LocaleListCompat] into a [List] of [Locale]s */
val LocaleListCompat.asList: List<Locale>
    get() = (0 until size()).mapNotNull { get(it) }
