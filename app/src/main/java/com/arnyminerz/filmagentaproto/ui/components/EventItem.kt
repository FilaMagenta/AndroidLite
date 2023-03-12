package com.arnyminerz.filmagentaproto.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.filmagentaproto.database.data.woo.Event
import com.ireward.htmlcompose.HtmlText
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun EventItem(event: Event, isConfirmed: Boolean, onClick: () -> Unit = {}) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(!isConfirmed, onClick = onClick),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    event.name,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                )
                if (isConfirmed)
                    Icon(Icons.Rounded.Star, "Confirmed")
            }
            HtmlText(
                event.shortDescription,
                modifier = Modifier.fillMaxWidth(),
                style = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f)
                ),
                // color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f),
                // fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            )
            val acceptsReservationsUntil = event.acceptsReservationsUntil
            acceptsReservationsUntil?.let {
                Text("Reservations: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(it)}")
            }
        }
    }
}
