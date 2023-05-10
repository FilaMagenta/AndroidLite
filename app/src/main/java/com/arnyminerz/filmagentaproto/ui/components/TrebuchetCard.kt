package com.arnyminerz.filmagentaproto.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EditCalendar
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.ui.screens.DateFormatter
import com.arnyminerz.filmagentaproto.ui.theme.AppTheme
import com.arnyminerz.filmagentaproto.utils.launchCalendarInsert
import java.sql.Date
import java.time.ZonedDateTime

@Composable
@ExperimentalMaterial3Api
fun TrebuchetCard(shoots: Boolean?, obtainedDate: ZonedDateTime?, expiresDate: ZonedDateTime?) {
    val context = LocalContext.current

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
            stringResource(R.string.profile_trebuchet),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
            fontSize = 22.sp,
        )
        ReadField(
            value = obtainedDate?.let { DateFormatter.format(it) },
            label = R.string.profile_trebuchet_obtained,
        )
        ReadField(
            value = expiresDate?.let { DateFormatter.format(it) },
            label = R.string.profile_trebuchet_expires,
            action = expiresDate?.let {
                Icons.Rounded.EditCalendar to { context.launchCalendarInsert(it.toLocalDateTime()) }
            }
        )
    }
}

@Preview
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TrebuchetCardPreview() {
    AppTheme {
        TrebuchetCard(true, ZonedDateTime.now(), ZonedDateTime.now())
    }
}
