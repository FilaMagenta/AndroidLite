package com.arnyminerz.filmagentaproto.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EuroSymbol
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.ui.components.Keyboard

@Composable
@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
fun PaymentBottomSheet(
    isLoading: Boolean,
    onPaymentRequested: (amount: Double, concept: String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            var amount by remember { mutableStateOf("") }
            var concept by remember { mutableStateOf("") }

            val keyboard = LocalSoftwareKeyboardController.current

            Text(
                text = stringResource(R.string.payments_make_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            )
            Text(
                text = stringResource(R.string.payments_make_message),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            )

            OutlinedTextField(
                value = concept,
                onValueChange = { concept = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                label = { Text(stringResource(R.string.payments_make_concept)) },
                singleLine = false,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Sentences,
                    autoCorrect = true,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions { keyboard?.hide() },
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                label = { Text(stringResource(R.string.payments_make_amount)) },
                placeholder = { Text(stringResource(R.string.payments_make_placeholder)) },
                singleLine = true,
                enabled = !isLoading,
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Rounded.EuroSymbol, stringResource(R.string.payments_make_euros))
                },
            )
            Keyboard(
                onKeyPressed = {
                    amount += it
                },
                onBackspacePressed = {
                    amount = amount.substring(0, amount.length - 1)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                buttonModifier = Modifier
                    .height(56.dp)
                    .padding(vertical = 4.dp),
                backspaceIconSize = 16.dp,
            )
            OutlinedButton(
                onClick = { onPaymentRequested(amount.toDouble(), concept) },
                enabled = !isLoading && amount.toDoubleOrNull() != null,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = 8.dp, end = 8.dp),
            ) {
                Text(stringResource(R.string.payments_make_action))
            }
        }
    }
}
