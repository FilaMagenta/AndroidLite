package com.arnyminerz.filamagenta.core

import com.arnyminerz.filamagenta.core.utils.ANSI_BLUE
import com.arnyminerz.filamagenta.core.utils.ANSI_CYAN
import com.arnyminerz.filamagenta.core.utils.ANSI_RED
import com.arnyminerz.filamagenta.core.utils.ANSI_RESET
import com.arnyminerz.filamagenta.core.utils.ANSI_YELLOW
import java.util.regex.Pattern

private val ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$")

private const val MAX_LOG_LENGTH = 4000
private const val MAX_TAG_LENGTH = 23

object Logger {

    private val explicitTag = ThreadLocal<String>()

    private val fqcnIgnore = listOf(
        Logger::class.java.name
    )

    private val tag: String?
        get() {
            var tag = explicitTag.get()
            if (tag != null) explicitTag.remove()
            if (tag == null) {
                tag = Throwable().stackTrace
                    .first { it.className !in fqcnIgnore }
                    .let(::createStackElementTag)
            }
            return tag
        }

    /**
     * Extract the tag which should be used for the message from the `element`. By default
     * this will use the class name without any anonymous class suffixes (e.g., `Foo$1`
     * becomes `Foo`).
     *
     * Note: This will not be called if a [manual tag][.tag] was specified.
     */
    private fun createStackElementTag(element: StackTraceElement): String? {
        var tag = element.className.substringAfterLast('.')
        val m = ANONYMOUS_CLASS.matcher(tag)
        if (m.find()) {
            tag = m.replaceAll("")
        }
        return if (tag.length <= MAX_TAG_LENGTH) tag else tag.substring(0, MAX_TAG_LENGTH)
    }

    fun v(message: String, vararg formatArgs: Any?) {
        println(ANSI_BLUE + "$tag/V: " + ANSI_RESET + message.format(formatArgs))
    }

    fun d(message: String, vararg formatArgs: Any?) {
        println(ANSI_BLUE + "$tag/D: " + ANSI_RESET + message.format(formatArgs))
    }

    fun i(message: String, vararg formatArgs: Any?) {
        println(ANSI_CYAN + "$tag/I: " + ANSI_RESET + message.format(formatArgs))
    }

    fun w(message: String, vararg formatArgs: Any?) {
        println(ANSI_YELLOW + "$tag/W: " + ANSI_RESET + message.format(formatArgs))
    }

    fun e(exception: Throwable, message: String, vararg formatArgs: Any?) {
        System.err.println(ANSI_RED + "$tag/E: " + message.format(formatArgs))
        System.err.println(ANSI_RED + "$tag/E: " + exception.stackTraceToString())
    }

    fun e(message: String, vararg formatArgs: Any?) {
        System.err.println(ANSI_RED + "$tag/E: " + ANSI_RESET + message.format(formatArgs))
    }
}
