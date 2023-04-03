package com.arnyminerz.filmagentaproto.account

import android.util.Base64
import com.arnyminerz.filamagenta.core.security.Passwords
import com.arnyminerz.filmagentaproto.database.remote.RemoteDatabaseInterface
import com.arnyminerz.filmagentaproto.exceptions.WrongCredentialsException
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import timber.log.Timber

object RemoteAuthentication {
    /**
     * Tries to login with the given credentials.
     * @return The stored hash for the given combination.
     * @throws WrongCredentialsException If the password is not correct.
     * @throws IllegalArgumentException If the given DNI doesn't have an associated password.
     * @throws NoSuchAlgorithmException If the algorithm is not available in the current system.
     * @throws InvalidKeySpecException If there's an error with the generated key spec.
     */
    fun login(dni: String, password: String): String {
        Timber.d("Getting remote hash for DNI=$dni")
        val pair = RemoteDatabaseInterface.getHashForDni(dni)
        if (pair != null) {
            Timber.d("Processing password...")
            val (hash, salt) = pair
            val passwordArray = password.toCharArray()
            val hashBytes = Base64.decode(hash, Base64.NO_WRAP)
            val saltBytes = Base64.decode(salt, Base64.NO_WRAP)
            Timber.d("Checking if password is correct...")
            if (Passwords.isExpectedPassword(passwordArray, saltBytes, hashBytes))
                return hash
            else {
                Timber.e("The password stored doesn't match the introduced one.")
                throw WrongCredentialsException("The introduced password is not correct.")
            }
        }
        Timber.w("DNI not found in the hashes database.")
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
        val hashStr = Base64.encodeToString(hash, Base64.NO_WRAP)
        val saltStr = Base64.encodeToString(salt, Base64.NO_WRAP)
        val updatedRows = RemoteDatabaseInterface.addHashForDni(dni, hashStr, saltStr)
        if (updatedRows <= 0)
            throw IllegalStateException("Updated $updatedRows rows.")

        return hashStr
    }
}