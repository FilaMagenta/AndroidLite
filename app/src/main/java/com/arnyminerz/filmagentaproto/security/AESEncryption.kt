package com.arnyminerz.filmagentaproto.security

import android.util.Base64
import androidx.annotation.VisibleForTesting
import com.arnyminerz.filamagenta.core.security.AESEncryptionModel
import com.arnyminerz.filmagentaproto.BuildConfig

object AESEncryption: AESEncryptionModel() {

    override val salt: String = Base64.encodeToString(BuildConfig.AES_SALT.toByteArray(), Base64.NO_WRAP)
    override val iv: String = Base64.encodeToString(BuildConfig.AES_IV.toByteArray(), Base64.NO_WRAP)

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    override fun getSecretKey(): String = Base64.encodeToString(BuildConfig.AES_KEY.toByteArray(), Base64.NO_WRAP)

    override fun decodeBase64(input: String): ByteArray = Base64.decode(input, Base64.NO_WRAP)

    override fun encodeBase64ToString(input: ByteArray): String = Base64.encodeToString(input, Base64.NO_WRAP)

}
