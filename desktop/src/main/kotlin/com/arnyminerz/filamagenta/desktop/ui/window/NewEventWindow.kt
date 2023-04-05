package com.arnyminerz.filamagenta.desktop.ui.window

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import com.arnyminerz.filamagenta.desktop.localization.Translations.getString
import com.arnyminerz.filamagenta.desktop.ui.pages.NewEventPage
import com.arnyminerz.filamagenta.desktop.ui.theme.ThemedDialog

@Composable
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
fun NewEventDialog(onCloseRequest: () -> Unit) {
    ThemedDialog(
        onCloseRequest,
        titleArgument = getString("new.event.title"),
        isUndecorated = true,
    ) {
        NewEventPage(onCloseRequest)
    }
}
