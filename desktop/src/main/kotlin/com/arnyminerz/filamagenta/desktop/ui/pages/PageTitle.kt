package com.arnyminerz.filamagenta.desktop.ui.pages

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.filamagenta.desktop.localization.Translations

@Composable
fun PageTitle(textKey: String) {
    Text(
        text = Translations.getString(textKey),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        fontSize = 38.sp,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp, start = 12.dp),
    )
}
