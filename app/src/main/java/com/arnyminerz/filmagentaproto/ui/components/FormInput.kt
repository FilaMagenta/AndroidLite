package com.arnyminerz.filmagentaproto.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@Composable
fun FormInput(
    value: String,
    onValueChange: (value: String) -> Unit,
    label: String,
    supportingText: String,
    autofillType: AutofillType,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    focusRequester: FocusRequester? = null,
    nextFocusRequester: FocusRequester? = null,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.Words,
    isPassword: Boolean = false,
    autoCorrect: Boolean = false,
    error: String? = null,
    onGo: (() -> Unit)? = null,
) {
    val autofillNode = AutofillNode(
        autofillTypes = listOf(autofillType),
        onFill = onValueChange,
    )
    val autofill = LocalAutofill.current

    LocalAutofillTree.current += autofillNode
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        supportingText = { Text(error ?: supportingText) },
        keyboardOptions = KeyboardOptions(
            capitalization = capitalization,
            autoCorrect = autoCorrect,
            keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Ascii,
            imeAction = if (nextFocusRequester != null) ImeAction.Next else ImeAction.Go,
        ),
        isError = error != null,
        enabled = enabled,
        singleLine = true,
        maxLines = 1,
        keyboardActions = KeyboardActions { nextFocusRequester?.requestFocus() ?: onGo?.invoke() },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier
            .fillMaxWidth()
            .let { mod -> focusRequester?.let { mod.focusRequester(it) } ?: mod }
            .onGloballyPositioned {
                autofillNode.boundingBox = it.boundsInWindow()
            }
            .onFocusChanged { focusState ->
                if (focusState.isFocused)
                    autofill?.requestAutofillForNode(autofillNode)
                else
                    autofill?.cancelAutofillForNode(autofillNode)
            }
            .then(modifier),
    )
}
