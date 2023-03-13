package com.arnyminerz.filmagentaproto.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arnyminerz.filmagentaproto.R

private const val keyboardValues = "123456789"

@Composable
fun Keyboard(
    onKeyPressed: (Char) -> Unit,
    onBackspacePressed: () -> Unit,
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier,
    backspaceIconSize: Dp = 10.dp,
) {
    Column(modifier = modifier) {
        val packs = (keyboardValues.length / 3)
        for (index in 0 until packs) Row(Modifier.fillMaxWidth()) {
            val chars = listOf(
                keyboardValues[index*3],
                keyboardValues[index*3 + 1],
                keyboardValues[index*3 + 2],
            )
            OutlinedButton(
                onClick = { onKeyPressed(chars[0]) },
                modifier = Modifier.padding(end = 4.dp).weight(1f).then(buttonModifier),
            ) { Text(chars[0].toString()) }
            OutlinedButton(
                onClick = { onKeyPressed(chars[1]) },
                modifier = Modifier.padding(horizontal = 4.dp).weight(1f).then(buttonModifier),
            ) { Text(chars[1].toString()) }
            OutlinedButton(
                onClick = { onKeyPressed(chars[2]) },
                modifier = Modifier.padding(start = 4.dp).weight(1f).then(buttonModifier),
            ) { Text(chars[2].toString()) }
        }
        Row(Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { onKeyPressed('.') },
                modifier = Modifier.padding(end = 4.dp).weight(1f).then(buttonModifier),
            ) { Text(".") }
            OutlinedButton(
                onClick = { onKeyPressed('0') },
                modifier = Modifier.padding(horizontal = 4.dp).weight(1f).then(buttonModifier),
            ) { Text("0") }
            OutlinedButton(
                onClick = onBackspacePressed,
                modifier = Modifier.padding(start = 4.dp).weight(1f).then(buttonModifier),
            ) {
                Icon(
                    Icons.Outlined.Backspace,
                    stringResource(R.string.keyboard_backspace),
                    modifier = Modifier.size(backspaceIconSize),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun KeyboardPreview() {
    Keyboard(
        onKeyPressed = {},
        onBackspacePressed = {},
    )
}
