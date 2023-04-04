package com.arnyminerz.filamagenta.desktop.storage

import com.arnyminerz.filamagenta.core.resources.PropertiesProvider
import java.io.File
import java.io.InputStream
import java.io.OutputStream

object LocalPropertiesStorage: PropertiesProvider(false) {
    private val file = File(dataDir, "local.properties")

    init {
        dataDir.mkdirs()
        if (!file.exists()) file.createNewFile()
    }

    override fun getInputStream(): InputStream = file.inputStream()

    override fun getOutputStream(): OutputStream = file.outputStream()
}