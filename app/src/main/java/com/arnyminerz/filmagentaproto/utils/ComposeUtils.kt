package com.arnyminerz.filmagentaproto.utils

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

fun Color.Companion.random(seed : Int = 0): Color {
    val random = Random(seed)
    return Color(random.nextInt(256), random.nextInt(256), random.nextInt(256), random.nextInt(256))
}
