package com.arnyminerz.filmagentaproto.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.filmagentaproto.BuildConfig

@Composable
fun SettingsScreen() {
    Column {
        Column(modifier = Modifier.fillMaxWidth().weight(1f)) {

        }
        Text(
            "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f),
        )
    }
}
