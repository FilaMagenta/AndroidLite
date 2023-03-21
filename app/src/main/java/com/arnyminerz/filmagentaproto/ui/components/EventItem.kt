package com.arnyminerz.filmagentaproto.ui.components

import androidx.annotation.UiThread
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.database.data.woo.Event
import com.arnyminerz.filmagentaproto.database.data.woo.Order
import com.arnyminerz.filmagentaproto.database.data.woo.StockStatus
import com.arnyminerz.filmagentaproto.ui.dialogs.EventBottomSheet
import com.arnyminerz.filmagentaproto.utils.launchCalendarInsert
import java.text.SimpleDateFormat
import java.util.Locale

private val dateFormat: SimpleDateFormat
    get() = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

@Composable
@ExperimentalMaterial3Api
fun EventItem(
    event: Event,
    isConfirmed: Boolean,
    @UiThread onDelete: (onComplete: () -> Unit) -> Unit,
    onSignUp: (metadata: List<Order.Metadata>, onComplete: () -> Unit) -> Unit,
) {
    val context = LocalContext.current

    val inStock = event.stockStatus == StockStatus.InStock || event.stockQuantity > 0

    var showingCard by remember { mutableStateOf(false) }
    if (showingCard)
        EventBottomSheet(
            event = event,
            onDismissRequest = { showingCard = false },
            onSubmit = onSignUp,
        )

    var isDeleting by remember { mutableStateOf(false) }
    var showingCancelDialog by remember { mutableStateOf(false) }
    if (showingCancelDialog)
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showingCancelDialog = false },
            title = { Text(stringResource(R.string.events_cancel_reservation_title)) },
            icon = {
                Icon(
                    Icons.Rounded.DeleteForever,
                    stringResource(R.string.events_cancel_reservation_title),
                )
            },
            text = {
                Text(
                    stringResource(
                        R.string.events_cancel_reservation_message,
                        event.title,
                    )
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !isDeleting,
                    onClick = {
                        isDeleting = true
                        onDelete {
                            isDeleting = false
                            showingCancelDialog = false
                        }
                    },
                ) { Text(stringResource(R.string.yes)) }
            },
            dismissButton = {
                TextButton(
                    enabled = !isDeleting,
                    onClick = { showingCancelDialog = false },
                ) { Text(stringResource(R.string.cancel)) }
            },
        )

    OutlinedCard(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable {
                if (isConfirmed)
                    showingCancelDialog = true
                else
                    showingCard = true
            },
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    event.title,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    textDecoration = if (!inStock) TextDecoration.LineThrough else TextDecoration.None,
                )
                event.eventDate?.let {
                    IconButton(
                        onClick = {
                            context.launchCalendarInsert(
                                begin = it,
                                title = event.title,
                                description = event.shortDescription,
                            )
                        },
                    ) {
                        Icon(Icons.Rounded.EditCalendar, stringResource(R.string.add_to_calendar))
                    }
                } ?: IconButton(enabled = false, onClick = { }) { }
            }

            if (!inStock)
                Text(
                    stringResource(R.string.events_error_stock),
                    color = MaterialTheme.colorScheme.error,
                )

            event.cutDescription.takeIf { it.isNotBlank() }?.let {
                HtmlText(
                    text = it,
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 14.sp,
                )
            }
            if (!isConfirmed) {
                val acceptsReservationsUntil = event.acceptsReservationsUntil
                acceptsReservationsUntil?.let {
                    Text(
                        text = stringResource(
                            R.string.events_reservations_until,
                            dateFormat.format(it),
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                    )
                }
            }

            event.eventDate?.let { eventDate ->
                Text(
                    text = stringResource(
                        R.string.events_event_date,
                        dateFormat.format(eventDate),
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                )
            }
        }
    }
}
