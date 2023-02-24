package com.arnyminerz.filmagentaproto.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.database.remote.RemoteServer
import com.arnyminerz.filmagentaproto.exceptions.WrongCredentialsException
import com.arnyminerz.filmagentaproto.ui.components.FormInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
fun LoginScreen(onLogin: (name: String, nif: String, token: String) -> Unit) {
    Column(
        Modifier.fillMaxSize(),
    ) {
        var username by remember { mutableStateOf("") }
        var nif by remember { mutableStateOf("") }

        val nifFocusRequester = remember { FocusRequester() }

        FormInput(
            value = username,
            onValueChange = {username = it},
            label = stringResource(R.string.login_name),
            supportingText = stringResource(R.string.login_name_aux),
            nextFocusRequester = nifFocusRequester,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            capitalization = KeyboardCapitalization.Characters,
        )
        FormInput(
            value = nif,
            onValueChange = {nif = it},
            label = stringResource(R.string.login_nif),
            supportingText = stringResource(R.string.login_nif_aux),
            focusRequester = nifFocusRequester,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            onGo = {

            },
        )
        Button(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val token = RemoteServer.login(username, nif)
                        onLogin(username, nif, token)
                    }catch (e: WrongCredentialsException) {
                        Log.e("LoginScreen", "Wrong credentials.")
                    }
                }
            }
        ) {
            Text("Login")
        }
    }
}
