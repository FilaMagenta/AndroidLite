package com.arnyminerz.filmagentaproto.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.database.remote.protos.Socio

@Composable
fun UnacceptedPolicyCard(socio: Socio, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        )
    ) {
        Text(
            text = stringResource(R.string.error_policy, socio.Nombre),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        )
    }
}
