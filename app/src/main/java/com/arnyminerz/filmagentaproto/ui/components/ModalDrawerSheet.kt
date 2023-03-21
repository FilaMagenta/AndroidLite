package com.arnyminerz.filmagentaproto.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource

data class ModalDrawerSheetItem(
    val icon: ImageVector,
    @StringRes val labelRes: Int,
    val onClick: () -> Unit,
) {
    companion object {
        val Divider = ModalDrawerSheetItem(Icons.Default.Close, 0) {}
    }
}

@Composable
fun ModalNavigationDrawer(
    items: List<ModalDrawerSheetItem>,
    drawerState: DrawerState,
    content: @Composable () -> Unit,
) {
    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                for (item in items)
                    if (item.labelRes == 0)
                        Divider()
                    else
                        NavigationDrawerItem(
                            label = {
                                Text(stringResource(item.labelRes))
                            },
                            icon = {
                                Icon(
                                    item.icon,
                                    stringResource(item.labelRes)
                                )
                            },
                            selected = false,
                            onClick = item.onClick,
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
            }
        },
        drawerState = drawerState,
        content = content,
    )
}
