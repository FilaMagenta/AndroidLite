package com.arnyminerz.filamagenta.desktop.ui.window

import androidx.annotation.WorkerThread
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.filamagenta.core.Logger
import com.arnyminerz.filamagenta.core.exception.WrongCredentialsException
import com.arnyminerz.filamagenta.core.security.Passwords
import com.arnyminerz.filamagenta.core.utils.doUi
import com.arnyminerz.filamagenta.core.utils.io
import com.arnyminerz.filamagenta.core.utils.isValidDni
import com.arnyminerz.filamagenta.core.utils.ui
import com.arnyminerz.filamagenta.desktop.localization.Translations.getString
import com.arnyminerz.filamagenta.desktop.remote.RemoteAuthentication
import com.arnyminerz.filamagenta.desktop.storage.LocalPropertiesStorage
import com.arnyminerz.filamagenta.desktop.storage.Properties.USER_DNI
import com.arnyminerz.filamagenta.desktop.storage.Properties.USER_TOKEN
import com.arnyminerz.filamagenta.desktop.ui.components.forms.FormField
import com.arnyminerz.filamagenta.desktop.ui.theme.ThemedWindow
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException
import kotlinx.coroutines.launch

private const val TAG = "LoginWindow"

/**
 * Provides a form for logging in. Requires Internet connection.
 * @param onCloseRequest Called when the user closes the window.
 */
@Composable
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
fun LoginWindow(
    onCloseRequest: () -> Unit,
    @WorkerThread onLogin: suspend (token: String) -> Unit,
) {
    ThemedWindow(
        onCloseRequest = onCloseRequest,
    ) {
        val scope = rememberCoroutineScope()
        val snackbarHost = remember { SnackbarHostState() }

        var dni by remember { mutableStateOf(LocalPropertiesStorage[USER_DNI]) }
        var password by remember { mutableStateOf(LocalPropertiesStorage[USER_TOKEN] ?: "") }
        var storePassword by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }

        fun showSnackbar(msgKey: String) = doUi {
            snackbarHost.showSnackbar(
                message = getString(msgKey),
                duration = SnackbarDuration.Short
            )
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHost) },
        ) { paddingValues ->
            fun performLogin() = scope.launch {
                isLoading = true
                if (dni?.isValidDni == false || password.length < Passwords.MIN_LENGTH) {
                    snackbarHost.showSnackbar(getString("form.login.error.incomplete"))
                    isLoading = false
                    return@launch
                }
                val userDni = dni!!
                io {
                    try {
                        Logger.d(TAG, "Logging in as $userDni...")
                        LocalPropertiesStorage["user.dni"] = userDni
                        val token = RemoteAuthentication.login(userDni, password)

                        Logger.d(TAG, "Credentials are correct!")
                        if (storePassword) {
                            Logger.d(TAG, "Writing token to local properties...")
                            LocalPropertiesStorage["user.token"] = token
                        }

                        onLogin(token)
                    } catch (e: WrongCredentialsException) {
                        showSnackbar("form.login.error.wrong")
                    } catch (e: IllegalStateException) {
                        showSnackbar("form.login.error.password")
                    } catch (e: NoSuchAlgorithmException) {
                        showSnackbar("form.login.error.algorithm")
                    } catch (e: InvalidKeySpecException) {
                        showSnackbar("form.login.error.key_spec")
                    } finally {
                        ui { isLoading = false }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 64.dp)
                        .padding(top = 64.dp)
                        .widthIn(max = 500.dp),
                ) {
                    Text(
                        text = getString("form.login.title"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 48.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp, start = 16.dp),
                    )

                    val dniFocusRequester = remember { FocusRequester() }
                    val pasFocusRequester = remember { FocusRequester() }

                    val isFormFine =
                        dni?.isValidDni == true && password.length > Passwords.MIN_LENGTH

                    FormField(
                        value = dni ?: "",
                        onValueChange = { dni = it },
                        label = getString("form.login.dni"),
                        thisFocusRequester = dniFocusRequester,
                        nextFocusRequester = pasFocusRequester,
                        error = getString("form.login.error.dni").takeIf { dni?.isValidDni == false },
                        enabled = !isLoading,
                    )
                    FormField(
                        value = password,
                        onValueChange = { password = it },
                        isPassword = true,
                        label = getString("form.login.password"),
                        thisFocusRequester = pasFocusRequester,
                        prevFocusRequester = dniFocusRequester,
                        onImeAction = { performLogin() },
                        enabled = !isLoading,
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { storePassword = !storePassword },
                    ) {
                        Checkbox(
                            checked = storePassword,
                            onCheckedChange = { storePassword = it },
                            enabled = !isLoading,
                        )
                        Text(
                            getString("form.login.remember_password"),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }

                    Text(
                        text = getString("form.login.info_storage"),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp, top = 16.dp),
                    ) {
                        AnimatedVisibility(isLoading, modifier = Modifier.padding(end = 8.dp)) {
                            CircularProgressIndicator()
                        }

                        OutlinedButton(
                            enabled = !isLoading && isFormFine,
                            onClick = { performLogin() },
                        ) { Text(getString("form.login.action")) }
                    }

                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}
