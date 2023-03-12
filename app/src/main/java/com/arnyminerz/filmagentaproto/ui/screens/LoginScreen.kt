package com.arnyminerz.filmagentaproto.ui.screens

import android.util.Log
import androidx.annotation.WorkerThread
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
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.database.remote.RemoteServer
import com.arnyminerz.filmagentaproto.exceptions.WrongCredentialsException
import com.arnyminerz.filmagentaproto.ui.components.FormInput
import com.arnyminerz.filmagentaproto.utils.doAsync
import com.arnyminerz.filmagentaproto.utils.toastAsync

@Composable
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
fun LoginScreen(
    modifier: Modifier = Modifier,
    @WorkerThread onLogin: (name: String, nif: String, token: String) -> Unit,
) {
    val context = LocalContext.current

    Column(
        Modifier.fillMaxSize().then(modifier),
    ) {
        var username by remember { mutableStateOf("") }
        var nif by remember { mutableStateOf("") }
        var fieldsEnabled by remember { mutableStateOf(true) }

        val nifFocusRequester = remember { FocusRequester() }

        fun performLogin() {
            fieldsEnabled = false
            doAsync {
                try {
                    val token = RemoteServer.login(username, nif)
                    onLogin(username, nif, token)
                } catch (e: WrongCredentialsException) {
                    Log.e("LoginScreen", "Wrong credentials.")

                    context.toastAsync(R.string.error_toast_wrong_credentials)
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
            value = username,
            onValueChange = {username = it},
            enabled = fieldsEnabled,
            label = stringResource(R.string.login_name),
            supportingText = stringResource(R.string.login_name_aux),
            nextFocusRequester = nifFocusRequester,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            capitalization = KeyboardCapitalization.Characters,
            autofillType = AutofillType.Username,
        )
        FormInput(
            value = nif,
            onValueChange = {nif = it},
            enabled = fieldsEnabled,
            label = stringResource(R.string.login_nif),
            supportingText = stringResource(R.string.login_nif_aux),
            focusRequester = nifFocusRequester,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            autofillType = AutofillType.Password,
            onGo = ::performLogin,
        )
        Button(
            onClick = ::performLogin,
            enabled = fieldsEnabled,
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
