package com.arnyminerz.filmagentaproto.utils.compat

import android.content.Intent
import android.os.Build
import android.os.Parcelable
import kotlin.reflect.KClass

@Suppress("DEPRECATION")
fun <T: Parcelable> Intent.getParcelableExtraCompat(name: String, kClass: KClass<T>): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        getParcelableExtra(name, kClass.java)
    else
        getParcelableExtra(name)
