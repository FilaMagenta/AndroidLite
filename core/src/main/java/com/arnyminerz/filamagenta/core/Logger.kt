package com.arnyminerz.filamagenta.core

import com.arnyminerz.filamagenta.core.utils.ANSI_BLUE
import com.arnyminerz.filamagenta.core.utils.ANSI_CYAN
import com.arnyminerz.filamagenta.core.utils.ANSI_RED
import com.arnyminerz.filamagenta.core.utils.ANSI_RESET
import com.arnyminerz.filamagenta.core.utils.ANSI_YELLOW

object Logger {

    fun v(tag: String, message: String, vararg formatArgs: Any?) {
        println(ANSI_BLUE + "$tag/V: " + ANSI_RESET + message.format(formatArgs))
    }

    fun d(tag: String, message: String, vararg formatArgs: Any?) {
        println(ANSI_BLUE + "$tag/D: " + ANSI_RESET + message.format(formatArgs))
    }

    fun i(tag: String, message: String, vararg formatArgs: Any?) {
        println(ANSI_CYAN + "$tag/I: " + ANSI_RESET + message.format(formatArgs))
    }

    fun w(tag: String, message: String, vararg formatArgs: Any?) {
        println(ANSI_YELLOW + "$tag/W: " + ANSI_RESET + message.format(formatArgs))
    }

    fun e(tag: String, exception: Throwable, message: String, vararg formatArgs: Any?) {
        System.err.println(ANSI_RED + "$tag/E: " + message.format(formatArgs))
        System.err.println(ANSI_RED + "$tag/E: " + exception.stackTraceToString())
    }

    fun e(tag: String, message: String, vararg formatArgs: Any?) {
        System.err.println(ANSI_RED + "$tag/E: " + ANSI_RESET + message.format(formatArgs))
    }
}
