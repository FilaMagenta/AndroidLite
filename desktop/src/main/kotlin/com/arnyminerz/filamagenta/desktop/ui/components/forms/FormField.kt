package com.arnyminerz.filamagenta.desktop.ui.components.forms

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean = true,
    isPassword: Boolean = false,
    thisFocusRequester: FocusRequester? = null,
    nextFocusRequester: FocusRequester? = null,
    prevFocusRequester: FocusRequester? = null,
    onImeAction: (() -> Unit)? = null,
    error: String? = null,
    showError: Boolean? = null,
    supportingText: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .let { modifier ->
                thisFocusRequester?.let { modifier.focusRequester(thisFocusRequester) } ?: modifier
            }
            .onKeyEvent {
                if (it.type == KeyEventType.KeyDown) when (it.key) {
                    Key.Tab -> {
                        if (it.isShiftPressed)
                            prevFocusRequester?.requestFocus()
                        else
                            nextFocusRequester?.requestFocus()
                        true
                    }
                    Key.Enter -> {
                        onImeAction?.invoke()
                        true
                    }
                    else -> false
                } else false
            },
        visualTransformation = if (isPassword)
            PasswordVisualTransformation()
        else
            VisualTransformation.None,
        singleLine = true,
        maxLines = 1,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword)
                KeyboardType.Password
            else
                KeyboardType.Text,
            imeAction = ImeAction.Next.takeIf { nextFocusRequester != null } ?: ImeAction.Done,
        ),
        keyboardActions = KeyboardActions {
            nextFocusRequester?.requestFocus() ?: run {
                onImeAction?.invoke()
            }
        },
        isError = showError == true || error != null,
        supportingText = {
            error?.let { Text(it) } ?: supportingText?.let { Text(it) }
        },
    )
}
