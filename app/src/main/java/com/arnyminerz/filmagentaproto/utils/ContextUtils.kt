package com.arnyminerz.filmagentaproto.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

fun Context.toast(@StringRes textRes: Int, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, textRes, duration).show()
