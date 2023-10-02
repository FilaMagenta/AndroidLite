package com.arnyminerz.filmagentaproto.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
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
import com.arnyminerz.filamagenta.core.database.data.woo.Event
import com.arnyminerz.filamagenta.core.database.data.woo.StockStatus.Companion.InStock
import com.arnyminerz.filamagenta.core.database.data.woo.order.OrderMetadata
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.ui.dialogs.EventBottomSheet
import com.arnyminerz.filmagentaproto.utils.launchCalendarInsert
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormat: DateTimeFormatter
    get() = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.getDefault())

@Composable
@ExperimentalMaterial3Api
fun EventItem(
    event: Event,
    isConfirmed: Boolean,
    isArchived: Boolean,
    onEventSelected: () -> Unit,
    onSignUp: (metadata: List<OrderMetadata>, onComplete: () -> Unit) -> Unit,
) {
    val context = LocalContext.current

    val inStock = event.stockStatus == InStock && event.stockQuantity > 0
    val hasPassed = event.hasPassed

    var showingCard by remember { mutableStateOf(false) }
    if (showingCard)
        EventBottomSheet(
            event = event,
            onDismissRequest = { showingCard = false },
            onSubmit = onSignUp,
        )

    OutlinedCard(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(enabled = isConfirmed || (inStock && hasPassed)) {
                if (isConfirmed)
                    onEventSelected()
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
                    color = MaterialTheme.colorScheme.primary.copy(
                        alpha = .3f.takeIf { isArchived } ?: 1f,
                    ),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                    textDecoration = if (!isConfirmed && (!inStock || !hasPassed))
                        TextDecoration.LineThrough
                    else
                        TextDecoration.None,
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

            if (!isConfirmed && !inStock)
                Text(
                    stringResource(R.string.events_error_stock),
                    color = MaterialTheme.colorScheme.error,
                )
            if (!isConfirmed && !hasPassed)
                Text(
                    stringResource(R.string.events_error_reservations),
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
