package com.arnyminerz.filamagenta.desktop.ui.components.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity

@Composable
@ExperimentalMaterial3Api
fun <T : Any> SettingsDropdown(
    value: T,
    onValueChanged: (T) -> Unit,
    items: Iterable<T>,
    modifier: Modifier = Modifier,
    label: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    converter: (T) -> String = { it.toString() },
) {
    var expanded by remember { mutableStateOf(false) }
    var dropDownWidth by remember { mutableStateOf(0) }

    val fieldFocusRequester = remember { FocusRequester() }

    Column(modifier) {
        OutlinedTextField(
            value = converter(value),
            onValueChange = { },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    if (it.isFocused) expanded = true
                }
                .focusRequester(fieldFocusRequester)
                .onSizeChanged { dropDownWidth = it.width },
            label = label?.let { { Text(it) } },
            leadingIcon = leadingIcon,
            trailingIcon = {
                Icon(
                    Icons.Rounded.ArrowDropDown,
                    label,
                    Modifier.clickable { expanded = !expanded },
                )
            },
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false; fieldFocusRequester.freeFocus() },
            modifier = Modifier
                .width(with(LocalDensity.current) { dropDownWidth.toDp() })
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(converter(item)) },
                    onClick = { onValueChanged(item); expanded = false }
                )
            }
        }
    }
}
