package com.arnyminerz.filmagentaproto.utils

private const val dniLetters = "TRWAGMYFPDXBNJZSQVHLCKE"

val String.dniLetter: Char
    get() {
        // 12345678A
        val num = substring(0, 8).toInt()
        val mod = num % 23
        return dniLetters[mod]
    }

val String.isValidDni: Boolean
    get() = length == 9 && dniLetter == get(8)
