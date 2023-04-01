package com.arnyminerz.filamagenta.core.security

import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

abstract class AESEncryptionModel {

    protected abstract val salt: String
    protected abstract val iv: String

    abstract fun getSecretKey(): String

    protected abstract fun decodeBase64(input: String): ByteArray

    protected abstract fun encodeBase64ToString(input: ByteArray): String

    fun encrypt(strToEncrypt: String): String? {
        try {
            val ivParameterSpec = IvParameterSpec(decodeBase64(iv))

            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val spec = PBEKeySpec(
                getSecretKey().toCharArray(),
                decodeBase64(salt),
                10000,
                256
            )
            val tmp = factory.generateSecret(spec)
            val secretKey = SecretKeySpec(tmp.encoded, "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
            return encodeBase64ToString(
                cipher.doFinal(strToEncrypt.toByteArray(Charsets.UTF_8)),
            )
        } catch (e: Exception) {
            println("Error while encrypting: $e")
        }
        return null
    }

    fun decrypt(strToDecrypt: String): String? {
        try {
            val ivParameterSpec = IvParameterSpec(decodeBase64(iv))

            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val spec = PBEKeySpec(
                getSecretKey().toCharArray(),
                decodeBase64(salt),
                10000,
                256
            )
            val tmp = factory.generateSecret(spec);
            val secretKey = SecretKeySpec(tmp.encoded, "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
            return String(cipher.doFinal(decodeBase64(strToDecrypt)))
        } catch (e: Exception) {
            println("Error while decrypting: $e");
        }
        return null
    }
}