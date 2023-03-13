package com.arnyminerz.filmagentaproto.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.database.data.woo.Event

@Composable
@ExperimentalMaterial3Api
fun EventBottomSheet(
    event: Event,
    onDismissRequest: () -> Unit,
    onSubmit: (variationId: Long, onComplete: () -> Unit) -> Unit,
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

        var selectedAttributeOption by remember { mutableStateOf(0) }

        // Only first attribute will be taken
        event.attributes
            .takeIf { it.isNotEmpty() }
            ?.get(0)
            ?.takeIf { it.visible && it.options.isNotEmpty() }
            ?.let { attribute ->
                val selectedOption = attribute.options[selectedAttributeOption]

                Text(
                    text = attribute.name,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
                OutlinedCard(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    for ((index, option) in attribute.options.withIndex()) {
                        ListItem(
                            headlineContent = { Text(option) },
                            colors = ListItemDefaults.colors(
                                containerColor = if (option == selectedOption)
                                    MaterialTheme.colorScheme.surfaceVariant
                                else
                                    Color.Unspecified,
                            ),
                            modifier = Modifier
                                .clickable { selectedAttributeOption = index }
                        )
                    }
                }

                // FIXME: Add dropdown when fixed
                // https://issuetracker.google.com/issues/272882500
                // https://issuetracker.google.com/issues/271430136
                // OutlinedDropdownField(
                //     value = option,
                //     label = attribute.name,
                //     options = attribute.options,
                // ) { option = it }
            }

        // var notes by remember { mutableStateOf("") }

        // FIXME: Change when fixed
        // https://issuetracker.google.com/issues/268380384
        // https://issuetracker.google.com/issues/272483584
        // https://issuetracker.google.com/issues/261572786
        // OutlinedTextField(
        //     value = notes,
        //     onValueChange = { notes = it },
        //     label = { Text(stringResource(R.string.events_modal_notes)) },
        //     keyboardOptions = KeyboardOptions(
        //         capitalization = KeyboardCapitalization.Sentences,
        //         keyboardType = KeyboardType.Text,
        //         autoCorrect = true,
        //     ),
        //     modifier = Modifier
        //         .fillMaxWidth()
        //         .padding(horizontal = 8.dp)
        //         .padding(bottom = 8.dp),
        // )

        var confirming by remember { mutableStateOf(false) }
        Button(
            onClick = {
                confirming = true
                onSubmit(event.variations[selectedAttributeOption]) {
                    confirming = false
                    onDismissRequest()
                }
            },
            modifier = Modifier
                .align(Alignment.End)
                .padding(8.dp),
        ) {
            Text(stringResource(R.string.events_modal_submit))
        }
    }
}
