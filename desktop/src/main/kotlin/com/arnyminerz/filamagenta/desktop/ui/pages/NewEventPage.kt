package com.arnyminerz.filamagenta.desktop.ui.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isBackPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import com.arnyminerz.filamagenta.core.utils.matches
import com.arnyminerz.filamagenta.desktop.localization.Translations.getString
import com.arnyminerz.filamagenta.desktop.ui.components.forms.FormField
import java.text.SimpleDateFormat
import java.util.Locale

private val dateFormat: SimpleDateFormat
    get() = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

@Composable
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
fun WindowScope.NewEventPage(
    onCloseRequested: () -> Unit
) {
    var eventName by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf<String?>(null) }

    val isError = eventName.isBlank() || !dateFormat.matches(eventDate)

    Scaffold(
        modifier = Modifier
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Press && event.buttons.isBackPressed) {
                            onCloseRequested()
                            event.changes.forEach { it.consume() }
                        }
                    }
                }
            },
        topBar = {
            WindowDraggableArea {
                CenterAlignedTopAppBar(
                    title = { Text(getString("new.event.title")) },
                    actions = {
                        IconButton(
                            onClick = onCloseRequested,
                        ) { Icon(Icons.Rounded.Close, getString("common.back")) }
                    },
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(visible = !isError) {
                ExtendedFloatingActionButton(
                    onClick = { /* TODO: Create event */ },
                    icon = {  },
                    text = { Text(getString("new.event.action")) },
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .widthIn(max = 500.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val nameFocusRequester = remember { FocusRequester() }
            val dateFocusRequester = remember { FocusRequester() }

            FormField(
                value = eventName,
                onValueChange = { eventName = it },
                label = getString("new.event.name"),
                thisFocusRequester = nameFocusRequester,
                nextFocusRequester = dateFocusRequester,
            )
            FormField(
                value = eventDate ?: "",
                onValueChange = { eventDate = it },
                label = getString("new.event.date"),
                supportingText = getString("new.event.date_summary"),
                showError = eventDate?.let { !dateFormat.matches(eventDate) },
                prevFocusRequester = nameFocusRequester,
                thisFocusRequester = dateFocusRequester,
            )
        }
    }
}