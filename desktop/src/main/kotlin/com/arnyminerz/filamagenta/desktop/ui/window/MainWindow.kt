package com.arnyminerz.filamagenta.desktop.ui.window

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ApplicationScope
import com.arnyminerz.filamagenta.desktop.localization.Translations.getString
import com.arnyminerz.filamagenta.desktop.storage.LocalPropertiesStorage
import com.arnyminerz.filamagenta.desktop.storage.Properties.USER_TOKEN
import com.arnyminerz.filamagenta.desktop.ui.components.navigation.NavigationRailAction
import com.arnyminerz.filamagenta.desktop.ui.components.navigation.NavigationRailItemData
import com.arnyminerz.filamagenta.desktop.ui.components.navigation.NavigationRailPage
import com.arnyminerz.filamagenta.desktop.ui.pages.EventsPage
import com.arnyminerz.filamagenta.desktop.ui.pages.SettingsPage
import com.arnyminerz.filamagenta.desktop.ui.theme.ThemedWindow

context (ApplicationScope)
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

        val snackbarState = SnackbarHostState()

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarState) },
        ) { paddingValues ->
            NavigationRailPage(
                items = listOf(
                    NavigationRailItemData(
                        Icons.Filled.CalendarMonth,
                        Icons.Outlined.CalendarMonth,
                        getString("navigation.events"),
                    ),
                    NavigationRailItemData(
                        Icons.Filled.People,
                        Icons.Outlined.People,
                        getString("navigation.users"),
                    ),
                    NavigationRailItemData(
                        Icons.Filled.Settings,
                        Icons.Outlined.Settings,
                        getString("navigation.settings"),
                    ),
                ),
                action = NavigationRailAction(
                    icon = Icons.Outlined.ExitToApp,
                    label = getString("form.login.logout"),
                    action = onLogout,
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) { page ->
                with(snackbarState) {
                    when (page) {
                        0 -> EventsPage()
                        2 -> SettingsPage()
                        else -> Text("Hello from page $page")
                    }
                }
            }
        }
    }
}
