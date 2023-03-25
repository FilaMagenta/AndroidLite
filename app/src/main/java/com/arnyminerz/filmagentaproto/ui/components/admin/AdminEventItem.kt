package com.arnyminerz.filmagentaproto.ui.components.admin

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.TableRestaurant
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.database.data.woo.Event
import com.arnyminerz.filmagentaproto.database.data.woo.Order
import java.util.Locale

private val dateFormatter: SimpleDateFormat
    get() = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

@Composable
fun DoubleRow(icon1: ImageVector, text1: String, icon2: ImageVector, text2: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
        ) {
            Icon(icon1, null)
            Text(
                text = text1,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .weight(1f),
        ) {
            Icon(icon2, null)
            Text(
                text = text2,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
@ExperimentalMaterial3Api
fun AdminEventItem(
    event: Event,
    orders: List<Order>,
    onPeopleListRequested: () -> Unit,
    onTicketsListRequested: () -> Unit,
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Text(
            text = event.title,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(top = 8.dp, bottom = 4.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        DoubleRow(
            icon1 = Icons.Outlined.CalendarMonth,
            text1 = event.eventDate?.let { dateFormatter.format(it) } ?: "--",
            icon2 = Icons.Outlined.EventBusy,
            text2 = event.acceptsReservationsUntil?.let { dateFormatter.format(it) } ?: "--",
        )
        DoubleRow(
            icon1 = Icons.Outlined.Groups,
            text1 = orders.size.toString(),
            icon2 = Icons.Outlined.TableRestaurant,
            text2 = "¿?",
        )
        OutlinedButton(
            onClick = onPeopleListRequested,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .padding(vertical = 2.dp, horizontal = 8.dp),
        ) {
            Icon(Icons.Rounded.ReceiptLong, null)
            Text(
                text = stringResource(R.string.admin_events_view_people),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
        }
        OutlinedButton(
            onClick = onTicketsListRequested,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 8.dp),
        ) {
            Icon(Icons.Rounded.PictureAsPdf, null)
            Text(
                text = stringResource(R.string.admin_events_export_tickets),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
        }
        // Final margin
        Spacer(Modifier.height(8.dp))
    }
}

@Preview(
    showBackground = true,
)
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AdminEventItemPreview() {
    AdminEventItem(
        event = Event.EXAMPLE,
        orders = emptyList(),
        onPeopleListRequested = {},
        onTicketsListRequested = {},
    )
}
