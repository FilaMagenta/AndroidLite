package com.arnyminerz.filmagentaproto.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.ui.theme.AppTheme

@Composable
private fun RowScope.TextCol(
    value: Double,
    @StringRes label: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        Modifier
            .weight(1f)
            .then(modifier),
    ) {
        Text(
            text = stringResource(label),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelMedium,
            fontSize = 16.sp,
        )
        Text(
            text = "%.2f €".format(value),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 18.sp,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = if (value > 0)
                colorResource(R.color.green)
            else if (value < 0)
                MaterialTheme.colorScheme.error
            else
                Color.Unspecified
        )
    }
}

@Composable
fun BalanceCard(inwards: Double, outwards: Double, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
    ) {
        Row(Modifier.padding(8.dp)) {
            TextCol(
                value = inwards + outwards,
                label = R.string.balance_balance,
                modifier = Modifier.padding(end = 4.dp),
            )
            TextCol(
                value = inwards,
                label = R.string.balance_in,
                modifier = Modifier.padding(end = 8.dp),
            )
            TextCol(
                value = outwards,
                label = R.string.balance_out,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}

@Preview
@Composable
fun BalanceCardPreview() {
    AppTheme {
        BalanceCard(235.0, -250.0)
    }
}
