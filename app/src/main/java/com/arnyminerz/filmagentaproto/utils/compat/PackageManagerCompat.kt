package com.arnyminerz.filmagentaproto.utils.compat

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

@Suppress("DEPRECATION")
fun PackageManager.queryIntentActivitiesCompat(intent: Intent, flags: Int) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(flags.toLong()))
    } else {
        queryIntentActivities(intent, flags)
    }

@Suppress("DEPRECATION")
fun PackageManager.resolveServiceCompat(intent: Intent, flags: Int) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        resolveService(intent, PackageManager.ResolveInfoFlags.of(flags.toLong()))
    } else {
        resolveService(intent, flags)
    }
