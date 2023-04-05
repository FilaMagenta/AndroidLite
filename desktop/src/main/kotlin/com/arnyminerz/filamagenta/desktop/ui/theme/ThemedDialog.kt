package com.arnyminerz.filamagenta.desktop.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowScope
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState

@Composable
fun ThemedDialog(
    onCloseRequest: () -> Unit,
    visible: Boolean = true,
    titleArgument: String? = null,
    dragOffset: DpOffset = DpOffset.Zero,
    isUndecorated: Boolean = false,
    initialSize: Pair<Int, Int> = 1000 to 700,
    content: @Composable DialogWindowScope.() -> Unit,
) {
    val (width, height) = initialSize

    Dialog(
        onCloseRequest = onCloseRequest,
        visible = visible,
        undecorated = isUndecorated,
        title = "Filà Magenta" + (titleArgument?.let { " - $it" } ?: ""),
        state = rememberDialogState(
            width = width.dp,
            height = height.dp,
            position = WindowPosition(
                x = dragOffset.x,
                y = dragOffset.y,
            ),
        ),
        icon = painterResource("icon.svg"),
    ) {
        AppTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .let { modifier ->
                        // Add border for undecorated windows
                        if (isUndecorated)
                            modifier.border(
                                BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground)
                            )
                        else
                            modifier
                    },
            ) {
                content()
            }
        }
    }
}
