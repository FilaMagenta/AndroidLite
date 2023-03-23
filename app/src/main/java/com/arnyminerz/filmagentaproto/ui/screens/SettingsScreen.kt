package com.arnyminerz.filmagentaproto.ui.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.arnyminerz.filmagentaproto.BuildConfig
import com.arnyminerz.filmagentaproto.Locator
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.service.LanguageChangeReceiver
import com.arnyminerz.filmagentaproto.ui.components.OutlinedDropdownField
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
            val languages by LanguageChangeReceiver.currentLanguage.observeAsState()

            OutlinedDropdownField(
                value = languages
                    ?.firstOrNull()
                    ?.let { it.getDisplayName(it).capitalized(it) }
                    ?: stringResource(R.string.settings_language_default),
                options = Locator.Locales.toList(),
                label = stringResource(R.string.settings_language),
                itemToString = { it.getDisplayName(it).capitalize(Locale.current) },
            ) { locale ->
                LanguageChangeReceiver.currentLanguage.postValue(
                    listOf(locale)
                )
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.create(locale)
                )
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
