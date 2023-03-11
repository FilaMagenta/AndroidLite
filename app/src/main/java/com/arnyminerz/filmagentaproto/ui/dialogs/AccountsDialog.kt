package com.arnyminerz.filmagentaproto.ui.dialogs

import android.accounts.Account
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import com.arnyminerz.filmagentaproto.R

@ExperimentalMaterial3Api
@Composable
fun AccountsDialog(
    accountsList: Array<out Account>,
    selectedAccountIndex: Int,
    onAccountSelected: (index: Int, account: Account) -> Unit,
    onNewAccountRequested: () -> Unit,
    onAccountRemoved: (account: Account) -> Unit,
    onDismissRequested: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequested,
        confirmButton = {},
        title = { Text(stringResource(R.string.accounts_list)) },
        text = {
            LazyColumn {
                itemsIndexed(accountsList) { index, account ->
                    ListItem(
                        leadingContent = {
                            Icon(
                                if (selectedAccountIndex == index)
                                    Icons.Rounded.RadioButtonChecked
                                else
                                    Icons.Rounded.RadioButtonUnchecked,
                                stringResource(R.string.accounts_new),
                            )
                        },
                        trailingContent = {
                            IconButton(onClick = { onAccountRemoved(account) }) {
                                Icon(Icons.Rounded.Remove, stringResource(R.string.accounts_remove))
                            }
                        },
                        headlineText = { Text(account.name.capitalize(Locale.current)) },
                        modifier = Modifier
                            .clickable {
                                onAccountSelected(index, account)
                            },
                    )
                }
                item {
                    ListItem(
                        leadingContent = {
                            Icon(Icons.Rounded.Add, stringResource(R.string.accounts_new))
                        },
                        headlineText = { Text(stringResource(R.string.accounts_new)) },
                        modifier = Modifier
                            .clickable { onNewAccountRequested() },
                    )
                }
            }
        }
    )
}
