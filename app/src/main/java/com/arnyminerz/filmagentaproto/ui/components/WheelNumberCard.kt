package com.arnyminerz.filmagentaproto.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.ui.theme.AppTheme

@Composable
private fun RowScope.WheelNumber(number: Int?, @StringRes label: Int) {
    Column(
        Modifier.weight(1f),
    ) {
        Text(
            text = stringResource(label),
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
        )
        Text(
            text = number?.toString() ?: stringResource(R.string.profile_wheel_no_number),
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
        )
    }
}

@Composable
fun WheelNumberCard(white: Int?, black: Int?, gloria: Int?) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Text(
            stringResource(R.string.profile_wheel_title),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
            fontSize = 22.sp,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(bottom = 8.dp),
        ) {
            WheelNumber(white, R.string.profile_wheel_whites)
            WheelNumber(black, R.string.profile_wheel_blacks)
            WheelNumber(gloria, R.string.profile_wheel_gloria)
        }
    }
}

@Preview
@Composable
fun WheelNumberCardPreview() {
    AppTheme {
        WheelNumberCard(white = 91, black = null, gloria = null)
    }
}
