package com.arnyminerz.filamagenta.desktop.ui.components.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BreakfastDining
import androidx.compose.material.icons.outlined.DinnerDining
import androidx.compose.material.icons.outlined.LunchDining
import androidx.compose.material.icons.outlined.NoMeals
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.filamagenta.core.database.data.woo.Event
import com.arnyminerz.filamagenta.core.database.data.woo.event.EventType
import com.arnyminerz.filamagenta.desktop.localization.Translations.getString
import java.text.SimpleDateFormat
import java.util.Locale

private val dateFormatter: SimpleDateFormat
    get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

@Composable
fun EventItem(event: Event) {
    OutlinedCard(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(4.dp),
    ) {
        Row {
            Icon(
                when (event.type) {
                    EventType.Breakfast -> Icons.Outlined.BreakfastDining
                    EventType.Lunch -> Icons.Outlined.LunchDining
                    EventType.Dinner -> Icons.Outlined.DinnerDining
                    else -> Icons.Outlined.NoMeals
                },
                getString("list.event.type"),
                modifier = Modifier
                    .size(42.dp)
                    .padding(start = 8.dp, top = 8.dp),
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(top = 8.dp),
                )
                Text(
                    text = event.slug,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(bottom = 8.dp),
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp),
        ) {
            Icon(Icons.Rounded.CalendarMonth, null)
            Text(
                text = getString(
                    "list.event.date",
                    event.eventDate?.let { dateFormatter.format(it) } ?: getString("common.not_set"),
                ),
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp),
        ) {
            Icon(Icons.Rounded.EventBusy, null)
            Text(
                text = getString(
                    "list.event.reservation",
                    event.acceptsReservationsUntil?.let { dateFormatter.format(it) } ?: getString("common.not_set"),
                ),
                style = MaterialTheme.typography.labelMedium,
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}
