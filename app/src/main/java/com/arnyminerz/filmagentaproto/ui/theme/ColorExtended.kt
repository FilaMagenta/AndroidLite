package com.arnyminerz.filmagentaproto.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Success = Color(0xFF3DC436)
val Warning = Color(0xFFDC8400)

val light_Success = Color(0xFF006E07)
val light_onSuccess = Color(0xFFFFFFFF)
val light_SuccessContainer = Color(0xFF79FE69)
val light_onSuccessContainer = Color(0xFF002201)

val dark_Success = Color(0xFF5CE150)
val dark_onSuccess = Color(0xFF003A02)
val dark_SuccessContainer = Color(0xFF005304)
val dark_onSuccessContainer = Color(0xFF79FE69)

val light_Warning = Color(0xFF8A5100)
val light_onWarning = Color(0xFFFFFFFF)
val light_WarningContainer = Color(0xFFFFDCBD)
val light_onWarningContainer = Color(0xFF2C1600)

val dark_Warning = Color(0xFFFFB86E)
val dark_onWarning = Color(0xFF492900)
val dark_WarningContainer = Color(0xFF693C00)
val dark_onWarningContainer = Color(0xFFFFDCBD)

val SuccessColor: Color
    @Composable
    get() = if (isSystemInDarkTheme())
        dark_Success
    else
        light_Success

val WarningColor: Color
    @Composable
    get() = if (isSystemInDarkTheme())
        dark_Warning
    else
        light_Warning
