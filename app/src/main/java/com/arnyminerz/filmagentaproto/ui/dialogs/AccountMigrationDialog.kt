package com.arnyminerz.filmagentaproto.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.arnyminerz.filmagentaproto.R

@Composable
@ExperimentalMaterial3Api
fun AccountMigrationDialog(onLoginRequested: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(R.string.dialog_account_migration_title)) },
        text = { Text(stringResource(R.string.dialog_account_migration_message))},
        confirmButton = {
            TextButton(onClick = onLoginRequested) {
                Text(stringResource(android.R.string.ok))
            }
        }
    )
}
