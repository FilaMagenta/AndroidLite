package com.arnyminerz.filmagentaproto.utils

import kotlin.math.pow

/**
 * Rounds the given number to a desired amount of decimal places
 */
fun Double.roundTo(decimalPlaces: Int) = (10.0).pow(decimalPlaces).let { mult ->
    (this * mult).toInt() / mult
}
