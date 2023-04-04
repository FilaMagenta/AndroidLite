package com.arnyminerz.filamagenta.desktop.ui.window

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arnyminerz.filamagenta.desktop.localization.Translations.get
import com.arnyminerz.filamagenta.desktop.storage.LocalPropertiesStorage
import com.arnyminerz.filamagenta.desktop.storage.Properties.USER_TOKEN
import com.arnyminerz.filamagenta.desktop.ui.components.navigation.NavigationRailAction
import com.arnyminerz.filamagenta.desktop.ui.components.navigation.NavigationRailItemData
import com.arnyminerz.filamagenta.desktop.ui.components.navigation.NavigationRailPage
import com.arnyminerz.filamagenta.desktop.ui.theme.ThemedWindow

@Composable
@ExperimentalMaterial3Api
fun MainWindow(
    onCloseRequest: () -> Unit,
    onLogout: () -> Unit,
) {
    ThemedWindow(
        onCloseRequest,
    ) {
        val token by LocalPropertiesStorage.getLive(USER_TOKEN).collectAsState(null)

        Scaffold { paddingValues ->
            NavigationRailPage(
                items = listOf(
                    NavigationRailItemData(
                        Icons.Outlined.CalendarMonth,
                        Icons.Filled.CalendarMonth,
                        get("navigation.events"),
                    ),
                    NavigationRailItemData(
                        Icons.Outlined.CalendarMonth,
                        Icons.Filled.CalendarMonth,
                        get("navigation.events"),
                    ),
                ),
                action = NavigationRailAction(
                    icon = Icons.Outlined.ExitToApp,
                    label = get("form.login.logout"),
                    action = onLogout,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) { page ->
                Text("Hello from page $page")
            }
        }
    }
}
