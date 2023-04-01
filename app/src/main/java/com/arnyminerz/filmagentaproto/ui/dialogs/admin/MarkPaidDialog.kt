package com.arnyminerz.filmagentaproto.ui.dialogs.admin

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.arnyminerz.filmagentaproto.R

@Composable
fun MarkPaidDialog(
    customerName: String,
    eventName: String,
    onConfirmRequest: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.admin_events_mark_paid_title)) },
        text = {
            Text(
                stringResource(
                    R.string.admin_events_mark_paid_message,
                    customerName,
                    eventName,
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmRequest) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}
