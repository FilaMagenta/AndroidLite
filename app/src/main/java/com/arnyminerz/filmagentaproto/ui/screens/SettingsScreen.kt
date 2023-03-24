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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.work.WorkInfo
import com.arnyminerz.filmagentaproto.BuildConfig
import com.arnyminerz.filmagentaproto.Locator
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.service.LanguageChangeReceiver
import com.arnyminerz.filmagentaproto.ui.components.OutlinedDropdownField
import com.arnyminerz.filmagentaproto.ui.components.settings.SettingsCategory
import com.arnyminerz.filmagentaproto.ui.components.settings.SettingsItem
import com.arnyminerz.filmagentaproto.utils.capitalized
import com.arnyminerz.filmagentaproto.worker.SyncWorker

@Composable
@ExperimentalMaterial3Api
fun SettingsScreen() {
    val context = LocalContext.current

    Column {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val languages by LanguageChangeReceiver.currentLanguage.observeAsState()

            SettingsCategory(
                text = stringResource(R.string.settings_category_general),
            )
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

            SettingsCategory(
                text = stringResource(R.string.settings_category_advanced),
            )
            val workInfos by SyncWorker.getLiveState(context).observeAsState()
            SettingsItem(
                headline = stringResource(R.string.settings_worker_title),
                supporting = when(
                    workInfos?.firstOrNull { it.tags.contains(SyncWorker.TAG_PERIODIC) }?.state
                ) {
                    WorkInfo.State.ENQUEUED -> stringResource(R.string.settings_worker_enqueued)
                    WorkInfo.State.RUNNING -> stringResource(R.string.settings_worker_running)
                    WorkInfo.State.SUCCEEDED -> stringResource(R.string.settings_worker_success)
                    null -> stringResource(R.string.settings_worker_not_scheduled)
                    else -> stringResource(R.string.settings_worker_failing)
                }
            )
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
