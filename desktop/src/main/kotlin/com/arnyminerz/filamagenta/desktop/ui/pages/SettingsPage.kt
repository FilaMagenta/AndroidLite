package com.arnyminerz.filamagenta.desktop.ui.pages

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import com.arnyminerz.filamagenta.core.Logger
import com.arnyminerz.filamagenta.core.utils.doAsync
import com.arnyminerz.filamagenta.desktop.localization.Translations
import com.arnyminerz.filamagenta.desktop.localization.Translations.getString
import com.arnyminerz.filamagenta.desktop.storage.LocalPropertiesStorage
import com.arnyminerz.filamagenta.desktop.storage.Properties
import com.arnyminerz.filamagenta.desktop.ui.components.settings.SettingsDropdown
import java.util.Locale
import kotlinx.coroutines.launch
import main

private const val TAG = "SettingsPage"

context(SnackbarHostState, ApplicationScope)
        @Composable
        @ExperimentalMaterial3Api
fun SettingsPage() {
    val scope = rememberCoroutineScope()

    PageTitle("list.settings.title")

    LazyVerticalGrid(
        columns = GridCells.Adaptive(400.dp),
    ) {
        item {
            var locale by remember { mutableStateOf(Locale.getDefault()) }

            SettingsDropdown(
                value = locale,
                onValueChanged = {
                    Locale.setDefault(it)
                    locale = it
                    LocalPropertiesStorage[Properties.LANGUAGE] = it.toLanguageTag()
                    scope.launch {
                        val result = showSnackbar(
                            message = getString("list.settings.snack.restart"),
                            actionLabel = getString("common.restart"),
                            duration = SnackbarDuration.Indefinite,
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            Logger.i(TAG, "Closing application...")
                            exitApplication()
                        }
                    }.invokeOnCompletion {
                        Logger.w(TAG, "Restarting application...")
                        doAsync { main() }
                    }
                },
                items = Translations.availableLocales ?: emptySet(),
                converter = { it.displayName },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                label = getString("list.settings.language"),
                leadingIcon = { Icon(Icons.Outlined.Language, getString("list.settings.language")) }
            )
        }
    }
}
