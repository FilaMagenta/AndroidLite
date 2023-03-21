package com.arnyminerz.filmagentaproto.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat

object PermissionsUtils {
    /**
     * Returns true if the user has given permission to send notifications, or the SDK level is
     * lower than Tiramisu.
     */
    fun hasNotificationPermission(context: Context) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        else
            true
}
