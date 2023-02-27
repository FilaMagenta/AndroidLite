package com.arnyminerz.filmagentaproto.ui.screens

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.arnyminerz.filmagentaproto.BuildConfig
import com.arnyminerz.filmagentaproto.Locator
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.service.LanguageChangeReceiver
import com.arnyminerz.filmagentaproto.utils.capitalized

@Composable
@ExperimentalMaterial3Api
fun SettingsScreen() {
    Column {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                var expanded by remember { mutableStateOf(false) }
                val languages by LanguageChangeReceiver.currentLanguage.observeAsState()

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = languages
                            ?.firstOrNull()
                            ?.let { it.getDisplayName(it).capitalized(it) }
                            ?: stringResource(R.string.settings_language_default),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        label = { Text(stringResource(R.string.settings_language)) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .menuAnchor(),
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        Locator.Locales.forEach { locale ->
                            DropdownMenuItem(
                                text = { Text(locale.getDisplayName(locale).capitalized(locale)) },
                                onClick = {
                                    LanguageChangeReceiver.currentLanguage.postValue(
                                        listOf(locale)
                                    )
                                    AppCompatDelegate.setApplicationLocales(
                                        LocaleListCompat.create(locale)
                                    )
                                    expanded = false
                                },
                            )
                        }
                    }
                }
            }
        }
        Text(
            "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f),
        )
    }
}
