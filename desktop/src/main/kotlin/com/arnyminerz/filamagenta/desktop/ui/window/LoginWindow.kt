package com.arnyminerz.filamagenta.desktop.ui.window

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.filamagenta.desktop.localization.Translations.get
import com.arnyminerz.filamagenta.desktop.ui.components.forms.FormField
import com.arnyminerz.filamagenta.desktop.ui.theme.ThemedWindow

/**
 * Provides a form for logging in. Requires Internet connection.
 * @param onCloseRequest Called when the user closes the window.
 */
@Composable
@ExperimentalMaterial3Api
fun LoginWindow(
    onCloseRequest: () -> Unit,
) {
    ThemedWindow(
        onCloseRequest = onCloseRequest,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 64.dp)
                .widthIn(max = 500.dp),
        ) {
            Text(
                text = get("form.login.title"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 48.sp,
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp, start = 16.dp),
            )

            var dni by remember { mutableStateOf("") }

            FormField(
                value = dni,
                onValueChange = { dni = it },
                label = get("form.login.dni"),
            )
        }
    }
}
