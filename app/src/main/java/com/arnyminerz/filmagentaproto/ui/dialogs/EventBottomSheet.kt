package com.arnyminerz.filmagentaproto.ui.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.database.data.woo.Event
import com.arnyminerz.filmagentaproto.ui.components.OutlinedDropdownField

@Composable
@ExperimentalMaterial3Api
fun EventBottomSheet(
    event: Event,
    onDismissRequest: () -> Unit,
    onSubmit: () -> Unit,
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = state,
    ) {
        Text(
            text = event.name,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            style = MaterialTheme.typography.titleMedium,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
        )
        event.attributes
            .takeIf { it.isNotEmpty() }
            ?.filter { it.visible && it.options.isNotEmpty() }
            ?.sortedBy { it.position }
            ?.forEach { attribute ->
                var option by remember { mutableStateOf(attribute.options[0]) }

                OutlinedDropdownField(
                    value = option,
                    label = attribute.name,
                    options = attribute.options,
                ) { option = it }
            }

        var notes by remember { mutableStateOf("") }

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.events_modal_notes)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                keyboardType = KeyboardType.Text,
                autoCorrect = true,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(bottom = 8.dp),
        )

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .align(Alignment.End)
                .padding(8.dp),
        ) {
            Text(stringResource(R.string.events_modal_submit))
        }
    }
}
