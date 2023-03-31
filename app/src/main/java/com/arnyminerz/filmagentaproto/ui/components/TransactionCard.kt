package com.arnyminerz.filmagentaproto.ui.components

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.database.data.Transaction

private val dateFormatter: SimpleDateFormat
    get() = SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

@Composable
fun TransactionCard(transaction: Transaction) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
    ) {
        Text(
            text = dateFormatter.format(transaction.date),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(top = 8.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = transaction.concept.lowercase().capitalize(Locale.current),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            Text(
                text = if (transaction.units > 1)
                    "%.2f € x %d".format(transaction.price, transaction.units)
                else
                    "%.2f €".format(transaction.price),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (transaction.income)
                    colorResource(R.color.green)
                else
                    MaterialTheme.colorScheme.error,
            )
        }
    }
}
