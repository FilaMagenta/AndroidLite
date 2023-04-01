package com.arnyminerz.filamagenta.core

object Logger {

    fun v(message: String, vararg formatArgs: Any?) {
        println(message.format(formatArgs))
    }

    fun d(message: String, vararg formatArgs: Any?) {
        println(message.format(formatArgs))
    }

    fun i(message: String, vararg formatArgs: Any?) {
        println(message.format(formatArgs))
    }
}
