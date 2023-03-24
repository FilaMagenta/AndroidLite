package com.arnyminerz.filmagentaproto.ui.components.settings

import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SettingsItem(headline: String, overline: String? = null, supporting: String? = null) {
    ListItem(
        headlineContent = { Text(headline) },
        overlineContent = overline?.let { { Text(it) } },
        supportingContent = supporting?.let { { Text(it) } },
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsItemPreview() {
    SettingsItem(
        headline = "Testing item",
        overline = "This is some overline",
        supporting = "Supporting text"
    )
}
