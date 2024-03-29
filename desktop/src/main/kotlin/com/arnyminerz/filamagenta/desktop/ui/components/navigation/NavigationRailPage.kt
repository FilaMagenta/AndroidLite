package com.arnyminerz.filamagenta.desktop.ui.components.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

data class NavigationRailItemData(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val selectedText: String,
    val unselectedText: String = selectedText,
)

data class NavigationRailAction(
    val icon: ImageVector,
    val label: String,
    val action: () -> Unit,
) {
    operator fun invoke() = action()
}

@Composable
fun NavigationRailPage(
    items: Iterable<NavigationRailItemData>,
    modifier: Modifier = Modifier,
    action: NavigationRailAction? = null,
    onPageSelected: (page: Int) -> Unit = {},
    content: @Composable ColumnScope.(page: Int) -> Unit,
) {
    Row(modifier) {
        var selectedPage by remember { mutableStateOf(0) }

        NavigationRail {
            for ((index, item) in items.withIndex())
                NavigationRailItem(
                    selected = selectedPage == index,
                    onClick = { selectedPage = index; onPageSelected(index) },
                    icon = {
                        Icon(
                            if (selectedPage == index)
                                item.selectedIcon
                            else
                                item.unselectedIcon,
                            if (selectedPage == index)
                                item.selectedText
                            else
                                item.unselectedText,
                        )
                    },
                    label = {
                        Text(
                            text = if (selectedPage == index)
                                item.selectedText
                            else
                                item.unselectedText,
                        )
                    },
                )
            Spacer(Modifier.weight(1f))
            action?.let { item ->
                NavigationRailItem(
                    selected = false,
                    onClick = { item() },
                    icon = { Icon(item.icon, item.label) },
                    label = { Text(item.label) },
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
        ) {
            content(selectedPage)
        }
    }
}
