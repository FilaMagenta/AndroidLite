package com.arnyminerz.filamagenta.core.security

import com.arnyminerz.filamagenta.core.utils.allLowerCase
import com.arnyminerz.filamagenta.core.utils.allUpperCase
import com.arnyminerz.filamagenta.core.utils.isNumber
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import java.util.Arrays
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

enum class PasswordSafety {
    /** The password given is safe */
    Safe,
    /** The password given is not safe because it contains things related to the Filà Magenta */
    Magenta,
    /** The password given is not safe because it's too short */
    Short,
    /** The password given is not safe because it contains only capital letters */
    AllCaps,
    /** The password given is not safe because it contains only lower case letters */
    AllLowercase,
    /** The password given is not safe because it only contains numbers. */
    AllNumbers,
}

/**
 * A utility class to hash passwords and check passwords vs hashed values. It uses a combination of hashing and unique
 * salt. The algorithm used is PBKDF2WithHmacSHA1 which, although not the best for hashing password (vs. bcrypt) is
 * still considered robust and <a href="https://security.stackexchange.com/a/6415/12614"> recommended by NIST </a>.
 * The hashed value has 256 bits.
 *
 * Java code taken from StackOverflow, converted to Kotlin by Arnau Mora.
 * @see <a href="https://stackoverflow.com/a/18143616/5717211">StackOverflow</a>
 */
object Passwords {
    private val RANDOM = SecureRandom()
    private const val ITERATIONS = 100000
    private const val KEY_LENGTH = 256

    /** The minimum password length */
    const val MIN_LENGTH = 8

    /** All the words forbidden for passwords because they break [PasswordSafety.Magenta] */
    private val FILA_TOPICS = listOf("1865", "magenta")

    /**
     * Returns a random salt to be used to hash a password.
     * @return a 16 bytes random salt
     */
    fun getNextSalt(): ByteArray {
        val salt = ByteArray(16)
        RANDOM.nextBytes(salt)
        return salt
    }

    /**
     * Returns a salted and hashed password using the provided hash.<br>
     * Note - side effect: the password is destroyed (the char[] is filled with zeros)
     *
     * @param password the password to be hashed
     * @param salt     a 16 bytes salt, ideally obtained with the getNextSalt method
     *
     * @throws NoSuchAlgorithmException If the algorithm is not available in the current system.
     * @throws InvalidKeySpecException If there's an error with the generated key spec.
     *
     * @return the hashed password with a pinch of salt
     */
    fun hash(password: CharArray, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH)
        Arrays.fill(password, Char.MIN_VALUE)
        try {
            val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            return skf.generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    /**
     * Returns true if the given password and salt match the hashed value, false otherwise.<br>
     * Note - side effect: the password is destroyed (the char[] is filled with zeros)
     *
     * @param password     the password to check
     * @param salt         the salt used to hash the password
     * @param expectedHash the expected hashed value of the password
     *
     * @throws NoSuchAlgorithmException If the algorithm is not available in the current system.
     * @throws InvalidKeySpecException If there's an error with the generated key spec.
     *
     * @return true if the given password and salt match the hashed value, false otherwise
     */
    fun isExpectedPassword(password: CharArray, salt: ByteArray, expectedHash: ByteArray): Boolean {
        val pwdHash = hash(password, salt)
        Arrays.fill(password, Char.MIN_VALUE)
        if (pwdHash.size != expectedHash.size) return false
        for (i in pwdHash.indices)
            if (pwdHash[i] != expectedHash[i]) return false
        return true
    }

    /**
     * Generates a random password of a given length, using letters and digits.
     *
     * @param length the length of the password
     *
     * @return a random password
     */
    fun generateRandomPassword(length: Int): String {
        val sb = StringBuilder(length)
        for (i in sb.indices) {
            val c = RANDOM.nextInt(62)
            if (c <= 9)
                sb.append(c.toString())
            else if (c < 39)
                sb.append('a' + c - 10)
            else
                sb.append('A' + c - 36)
        }
        return sb.toString()
    }

    /**
     * Checks if a given password is safe or not.
     */
    fun isSafePassword(password: String): PasswordSafety =
        when {
            password.length < MIN_LENGTH -> PasswordSafety.Short
            password.allLowerCase -> PasswordSafety.AllLowercase
            password.allUpperCase -> PasswordSafety.AllCaps
            password.isNumber -> PasswordSafety.AllNumbers
            FILA_TOPICS.any { password.contains(it, true) } -> PasswordSafety.Magenta
            else -> PasswordSafety.Safe
        }
}
