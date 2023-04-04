package com.arnyminerz.filmagentaproto.ui.screens

import androidx.annotation.WorkerThread
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arnyminerz.filamagenta.core.exception.WrongCredentialsException
import com.arnyminerz.filamagenta.core.security.PasswordSafety
import com.arnyminerz.filamagenta.core.security.Passwords
import com.arnyminerz.filamagenta.core.utils.doAsync
import com.arnyminerz.filamagenta.core.utils.isValidDni
import com.arnyminerz.filamagenta.core.utils.ui
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.account.RemoteAuthentication
import com.arnyminerz.filmagentaproto.security.labelRes
import com.arnyminerz.filmagentaproto.ui.components.FormInput
import com.arnyminerz.filmagentaproto.utils.toastAsync
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import timber.log.Timber

@Composable
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
fun LoginScreen(
    modifier: Modifier = Modifier,
    initialDni: String? = null,
    @WorkerThread onLogin: (name: String, nif: String, token: String) -> Unit,
) {
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .then(modifier),
    ) {
        // TODO: Verify DNI
        var dni by remember { mutableStateOf(initialDni ?: "") }
        var password by remember { mutableStateOf("") }
        var passwordConfirmation by remember { mutableStateOf<String?>(null) }
        var fieldsEnabled by remember { mutableStateOf(true) }
        var shouldCreateAccount by remember { mutableStateOf(false) }

        val passwordFocusRequester = remember { FocusRequester() }
        val repeatPasswordFocusRequester = remember { FocusRequester() }

        val safePassword = if (shouldCreateAccount) Passwords.isSafePassword(password) else null

        fun performLogin() {
            if (safePassword != null && safePassword != PasswordSafety.Safe) return

            fieldsEnabled = false
            doAsync {
                if (!shouldCreateAccount)
                    try {
                        Timber.d("Logging in remotely as $dni...")
                        val hash = RemoteAuthentication.login(dni, password)
                        Timber.d("Logged in correctly, running callback...")
                        onLogin(dni, password, hash)
                    } catch (e: WrongCredentialsException) {
                        Timber.e("Wrong credentials.")

                        context.toastAsync(R.string.error_toast_wrong_credentials)
                    } catch (e: IllegalArgumentException) {
                        Timber.w("The given user doesn't have an stored hash.")
                        ui { shouldCreateAccount = true }
                    } catch (e: NoSuchAlgorithmException) {
                        Timber.e(e, "The algorithm to use is not available")
                        // TODO: Warn the user
                    } catch (e: InvalidKeySpecException) {
                        Timber.e(e, "The key spec is not valid.")
                        // TODO: Warn the user
                    }
                else
                    try {
                        Timber.d("Registering $dni...")
                        val hash = RemoteAuthentication.register(dni, password)
                        Timber.d("Logged in correctly, running callback...")
                        onLogin(dni, password, hash)
                    } catch (e: IllegalArgumentException) {
                        Timber.e(e, "Got invalid response from server.")
                        // TODO: Warn the user
                    } catch (e: NoSuchAlgorithmException) {
                        Timber.e(e, "The algorithm to use is not available")
                        // TODO: Warn the user
                    } catch (e: InvalidKeySpecException) {
                        Timber.e(e, "The key spec is not valid.")
                        // TODO: Warn the user
                    }
            }.invokeOnCompletion { fieldsEnabled = true }
        }

        AsyncImage(
            model = R.mipmap.ic_launcher,
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier
                .size(96.dp)
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp),
        )

        FormInput(
            value = dni,
            onValueChange = { dni = it.uppercase() },
            enabled = fieldsEnabled,
            label = stringResource(R.string.login_nif),
            supportingText = stringResource(R.string.login_nif_aux),
            error = safePassword
                ?.takeIf { it != PasswordSafety.Safe }
                ?.labelRes
                ?.let { stringResource(it) },
            nextFocusRequester = passwordFocusRequester,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            capitalization = KeyboardCapitalization.Characters,
            autofillType = AutofillType.Username,
        )
        FormInput(
            value = password,
            onValueChange = { password = it },
            enabled = fieldsEnabled,
            label = stringResource(R.string.login_password),
            supportingText = stringResource(R.string.login_password_aux),
            focusRequester = passwordFocusRequester,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            autofillType = if (shouldCreateAccount) AutofillType.NewPassword else AutofillType.Password,
            isPassword = true,
            nextFocusRequester = repeatPasswordFocusRequester.takeIf { shouldCreateAccount },
            onGo = (::performLogin).takeIf { !shouldCreateAccount },
        )
        AnimatedVisibility(visible = shouldCreateAccount) {
            FormInput(
                value = passwordConfirmation ?: "",
                onValueChange = { passwordConfirmation = it },
                enabled = fieldsEnabled,
                label = stringResource(R.string.login_password_confirm),
                supportingText = stringResource(R.string.login_password_confirm_aux),
                focusRequester = repeatPasswordFocusRequester,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                capitalization = KeyboardCapitalization.Characters,
                isPassword = true,
                autofillType = AutofillType.NewPassword,
                error = stringResource(R.string.login_password_confirm_match).takeIf { passwordConfirmation != null && password != passwordConfirmation },
                onGo = ::performLogin,
            )
        }
        Button(
            onClick = ::performLogin,
            enabled = fieldsEnabled &&
                    password.length >= Passwords.MIN_LENGTH &&
                    dni.isValidDni &&
                    (if (shouldCreateAccount) password == passwordConfirmation else true) &&
                    (safePassword == null || safePassword == PasswordSafety.Safe),
            modifier = Modifier
                .align(Alignment.End)
                .padding(end = 8.dp),
        ) {
            Text(stringResource(R.string.login_action))
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
fun LoginScreenPreview() {
    LoginScreen { _, _, _ -> }
}
