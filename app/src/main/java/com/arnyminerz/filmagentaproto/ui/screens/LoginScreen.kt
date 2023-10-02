package com.arnyminerz.filmagentaproto.ui.screens

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arnyminerz.filamagenta.core.security.PasswordSafety
import com.arnyminerz.filamagenta.core.security.Passwords
import com.arnyminerz.filamagenta.core.utils.doAsync
import com.arnyminerz.filamagenta.core.utils.isValidDni
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.security.labelRes
import com.arnyminerz.filmagentaproto.ui.components.FormInput

@Composable
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
fun LoginScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        Modifier
            .fillMaxSize()
            .then(modifier),
    ) {
        var isLoading by remember { mutableStateOf(false) }

        fun performLogin() {
            isLoading = false

            val intent = CustomTabsIntent.Builder()
                .build()
            intent.launchUrl(
                context,
                Uri.parse(
                    "https://filamagenta.com/oauth/authorize/?client_id=QcQIPJWtmv8ViPdlJ8enJnMjH31HAOt5grgShShj&redirect_uri=app://filamagenta&response_type=code"
                )
            )

            isLoading = true
        }

        AsyncImage(
            model = R.mipmap.ic_launcher,
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier
                .size(96.dp)
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp),
        )

        Button(
            onClick = ::performLogin,
            enabled = !isLoading,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(.7f),
        ) {
            Text(stringResource(R.string.login_action))
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
fun LoginScreenPreview() {
    LoginScreen()
}
