package com.arnyminerz.filamagenta.core.resources

import java.io.InputStream
import java.io.OutputStream

open class ResourcesProperties(private val path: String): PropertiesProvider(true) {
    override fun getInputStream(): InputStream? = this::class.java.classLoader
        .getResourceAsStream(path)

    override fun getOutputStream(): OutputStream? = null
}
