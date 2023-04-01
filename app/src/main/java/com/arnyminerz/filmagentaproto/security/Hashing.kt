package com.arnyminerz.filmagentaproto.security

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/** Provides utility functions for hashing strings. */
object Hashing {
    /** Converts the given [ByteArray] ([bytes]) into a String. */
    private fun bytesToHexString(bytes: ByteArray) = StringBuffer().apply {
        for (byte in bytes) {
            val hex = Integer.toHexString(0xFF and byte.toInt())
            if (hex.length == 1)
                append('0')
            append(hex)
        }
    }.toString()

    /**
     * Hashes the given string using the SHA-256 algorithm.
     * @param string The string to hash.
     * @throws NoSuchAlgorithmException If the SHA-256 algorithm is not available on the system
     */
    fun sha256(string: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(string.toByteArray())

        return bytesToHexString(digest.digest())
    }
}