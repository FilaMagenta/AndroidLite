package com.arnyminerz.filmagentaproto.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateMapOf
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
import com.arnyminerz.filamagenta.core.database.data.woo.EventProto
import com.arnyminerz.filamagenta.core.database.data.woo.order.OrderMetadata
import com.arnyminerz.filmagentaproto.R
import timber.log.Timber

@Composable
@ExperimentalMaterial3Api
fun EventBottomSheet(
    event: EventProto,
    onDismissRequest: () -> Unit,
    onSubmit: (metadata: List<OrderMetadata>, onComplete: () -> Unit) -> Unit,
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isConfirming by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = { if (!isConfirming) onDismissRequest() },
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

        val selectedAttributesOption = remember { mutableStateMapOf<Long, OrderMetadata>() }

        // Only first attribute will be taken
        event.attributes
            .filter { it.options.isNotEmpty() }
            .forEach { attribute ->
                val selectedAttributeOption = selectedAttributesOption.getOrPut(
                    attribute.id,
                ) { attribute.toMetadata() }

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
                    for (option in attribute.options)
                        ListItem(
                            headlineContent = {
                                Text(
                                    if (option.price > 0.0)
                                        "%s (%.2f €)".format(option.displayValue, option.price)
                                    else
                                        option.displayValue
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = if (selectedAttributeOption.value == option.value)
                                    MaterialTheme.colorScheme.surfaceVariant
                                else
                                    Color.Unspecified,
                            ),
                            modifier = Modifier
                                .clickable {
                                    selectedAttributesOption[attribute.id] =
                                        attribute.toMetadata(option)
                                }
                        )
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

        Row(
            modifier = Modifier
                .align(Alignment.End)
                .padding(horizontal = 8.dp)
                .padding(top = 8.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedVisibility(
                visible = isConfirming,
                modifier = Modifier.padding(end = 8.dp)
            ) { CircularProgressIndicator() }
            Button(
                enabled = !isConfirming,
                onClick = {
                    isConfirming = true
                    Timber.d("Submitting event ${event.id}. Selected: $selectedAttributesOption")
                    onSubmit(selectedAttributesOption.values.toList()) {
                        isConfirming = false
                        onDismissRequest()
                    }
                },
            ) { Text(stringResource(R.string.events_modal_submit)) }
        }
    }
}
