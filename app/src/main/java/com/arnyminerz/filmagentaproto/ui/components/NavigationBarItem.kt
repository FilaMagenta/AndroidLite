package com.arnyminerz.filmagentaproto.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class NavigationBarItem(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val label: Int,
)

@Composable
fun RowScope.NavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    @StringRes label: Int
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(if(selected) selectedIcon else unselectedIcon, stringResource(label))
        },
        label = { Text(stringResource(label)) },
    )
}

@Composable
fun RowScope.NavigationBarItems(
    selectedIndex: Int,
    onSelected: suspend CoroutineScope.(index: Int) -> Unit,
    items: List<NavigationBarItem>,
) {
    val scope = rememberCoroutineScope()
    for ((index, item) in items.withIndex())
        NavigationBarItem(
            selected = selectedIndex == index,
            onClick = { scope.launch { onSelected(index) } },
            selectedIcon = item.selectedIcon,
            unselectedIcon = item.unselectedIcon,
            label = item.label,
        )
}
