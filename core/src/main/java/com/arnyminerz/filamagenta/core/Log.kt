package com.arnyminerz.filamagenta.core

object Logger {

    fun v(message: String, vararg formatArgs: Any?) {
        println("V: " + message.format(formatArgs))
    }

    fun d(message: String, vararg formatArgs: Any?) {
        println("D: " + message.format(formatArgs))
    }

    fun i(message: String, vararg formatArgs: Any?) {
        println("I: " + message.format(formatArgs))
    }

    fun w(message: String, vararg formatArgs: Any?) {
        println("W: " + message.format(formatArgs))
    }

    fun e(message: String, vararg formatArgs: Any?) {
        System.err.println("E: " + message.format(formatArgs))
    }
}
