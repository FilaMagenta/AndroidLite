package com.arnyminerz.filamagenta.core.remote

import com.arnyminerz.filamagenta.core.Logger
import com.arnyminerz.filamagenta.core.exception.WrongCredentialsException
import com.arnyminerz.filamagenta.core.security.Passwords
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException

abstract class RemoteAuthenticationProto(
    private val remoteDatabaseInterface: RemoteDatabaseInterfaceProto
) {
    protected abstract fun base64Encode(input: ByteArray): String

    protected abstract fun base64Decode(input: String): ByteArray

    /**
     * Tries to login with the given credentials.
     * @return The stored hash for the given combination.
     * @throws WrongCredentialsException If the password is not correct.
     * @throws IllegalArgumentException If the given DNI doesn't have an associated password.
     * @throws NoSuchAlgorithmException If the algorithm is not available in the current system.
     * @throws InvalidKeySpecException If there's an error with the generated key spec.
     */
    fun login(dni: String, password: String): String {
        Logger.d("Getting remote hash for DNI=$dni")
        val pair = remoteDatabaseInterface.getHashForDni(dni)
        if (pair != null) {
            Logger.d("Processing password...")
            val (hash, salt) = pair
            val passwordArray = password.toCharArray()
            val hashBytes = base64Decode(hash)
            val saltBytes = base64Decode(salt)
            Logger.d("Checking if password is correct...")
            if (Passwords.isExpectedPassword(passwordArray, saltBytes, hashBytes))
                return hash
            else {
                Logger.e("The password stored doesn't match the introduced one.")
                throw WrongCredentialsException("The introduced password is not correct.")
            }
        }
        Logger.w("DNI not found in the hashes database.")
        throw IllegalArgumentException("Could not find an stored hash for the given dni.")
    }

    /**
     * Registers a new user with the given DNI and password.
     * @return The generated hash for the given user.
     * @throws IllegalStateException If the response from the remote database returned an invalid
     * response (updated rows <= 0).
     * @throws NoSuchAlgorithmException If the algorithm is not available in the current system.
     * @throws InvalidKeySpecException If there's an error with the generated key spec.
     */
    fun register(dni: String, password: String): String {
        val salt = Passwords.getNextSalt()
        val hash = Passwords.hash(password.toCharArray(), salt)
        val hashStr = base64Encode(hash)
        val saltStr = base64Encode(salt)
        val updatedRows = remoteDatabaseInterface.addHashForDni(dni, hashStr, saltStr)
        if (updatedRows <= 0)
            throw IllegalStateException("Updated $updatedRows rows.")

        return hashStr
    }
}