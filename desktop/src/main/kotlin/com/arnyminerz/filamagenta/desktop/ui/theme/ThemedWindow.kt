package com.arnyminerz.filamagenta.desktop.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.zIndex

@Composable
fun ThemedWindow(
    onCloseRequest: () -> Unit,
    visible: Boolean = true,
    titleArgument: String? = null,
    initialSize: Pair<Int, Int> = 1000 to 700,
    content: @Composable FrameWindowScope.() -> Unit,
) {
    val (width, height) = initialSize

    Window(
        onCloseRequest = onCloseRequest,
        visible = visible,
        title = "Filà Magenta" + (titleArgument?.let { " - $it" } ?: ""),
        state = rememberWindowState(width = width.dp, height = height.dp),
        icon = painterResource("icon.svg"),
    ) {
        AppTheme {
            Box {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(0.dp, 0.dp, 0.dp, 8.dp),
                    modifier = Modifier.align(Alignment.TopEnd).zIndex(9999f),
                ) {
                    Text(
                        text = "BETA",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                    )
                }

                content()
            }
        }
    }
}
