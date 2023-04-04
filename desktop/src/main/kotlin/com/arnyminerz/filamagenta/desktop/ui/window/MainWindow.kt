package com.arnyminerz.filamagenta.desktop.ui.window

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import com.arnyminerz.filamagenta.core.database.data.woo.ROLE_ADMINISTRATOR
import com.arnyminerz.filamagenta.desktop.localization.Translations.getString
import com.arnyminerz.filamagenta.desktop.remote.RemoteCommerce
import com.arnyminerz.filamagenta.desktop.storage.LocalPropertiesStorage
import com.arnyminerz.filamagenta.desktop.storage.Properties.USER_DNI
import com.arnyminerz.filamagenta.desktop.storage.Properties.USER_TOKEN
import com.arnyminerz.filamagenta.desktop.ui.components.navigation.NavigationRailAction
import com.arnyminerz.filamagenta.desktop.ui.components.navigation.NavigationRailItemData
import com.arnyminerz.filamagenta.desktop.ui.components.navigation.NavigationRailPage
import com.arnyminerz.filamagenta.desktop.ui.pages.EventsPage
import com.arnyminerz.filamagenta.desktop.ui.pages.SettingsPage
import com.arnyminerz.filamagenta.desktop.ui.theme.ThemedWindow
import kotlinx.coroutines.flow.filterNotNull

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
        val dni by LocalPropertiesStorage.getLive(USER_DNI).collectAsState(null)
        var isAdmin by remember { mutableStateOf<Boolean?>(null) }

        LaunchedEffect(dni) {
            val customersList = RemoteCommerce.customersList()

            snapshotFlow { dni }
                .filterNotNull()
                .collect {
                    val customer = customersList
                        .find { it.username == dni } ?: return@collect onLogout()
                    isAdmin = customer.role == ROLE_ADMINISTRATOR
                }
        }

        val snackbarState = SnackbarHostState()

        when (isAdmin) {
            null -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            false -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                OutlinedCard(Modifier.widthIn(max = 700.dp)) {
                    Text(
                        text = getString("card.unauthorized.title"),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .padding(top = 8.dp),
                    )
                    Text(
                        text = getString("card.unauthorized.message"),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 8.dp),
                    )
                }
            }
            true -> {
                var currentPage by remember { mutableStateOf(0) }
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarState) },
                    floatingActionButton = {
                        AnimatedVisibility(
                            visible = currentPage == 0,
                            enter = slideInHorizontally(tween(200)) { it } + fadeIn(),
                            exit = slideOutVertically(tween(200)) { it } + fadeOut(),
                        ) {
                            FloatingActionButton(
                                onClick = {},
                            ) { Icon(Icons.Rounded.EditCalendar, getString("list.event.create")) }
                        }
                        AnimatedVisibility(
                            visible = currentPage == 1,
                            enter = slideInHorizontally(tween(200)) { it } + fadeIn(),
                            exit = slideOutVertically(tween(200)) { it } + fadeOut(),
                        ) {
                            FloatingActionButton(
                                onClick = {},
                            ) { Icon(Icons.Rounded.PersonAdd, getString("list.user.create")) }
                        }
                    },
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
                        onPageSelected = { currentPage = it },
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
    }
}
