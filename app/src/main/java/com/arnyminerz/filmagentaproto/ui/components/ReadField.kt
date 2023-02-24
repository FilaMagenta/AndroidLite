package com.arnyminerz.filmagentaproto.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
@ExperimentalMaterial3Api
fun ReadField(
    value: String?,
    @StringRes label: Int,
    action: Pair<ImageVector, () -> Unit>? = null,
) {
    if (value == null) return

    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        label = { Text(stringResource(label)) },
        trailingIcon = action?.let { (icon, onClick) ->
            {
                IconButton(onClick = onClick) {
                    Icon(icon, value)
                }
            }
        },
    )
}
