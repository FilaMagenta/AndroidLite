package com.arnyminerz.filmagentaproto.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import com.arnyminerz.filmagentaproto.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

val poppins = GoogleFont("Poppins")

val poppinsFamily = FontFamily(
    Font(googleFont = poppins, fontProvider = provider)
)
