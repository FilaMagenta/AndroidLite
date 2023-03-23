package com.arnyminerz.filmagentaproto.utils

data class Choose <T>(
    val ifTrue: T,
    val ifFalse: T,
)

infix fun <T> T.choose(ifFalse: T): Choose<T> = Choose(this, ifFalse)

infix fun <T> Choose<T>.by(value: Boolean): T = if (value) ifTrue else ifFalse
