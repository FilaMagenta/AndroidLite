package com.arnyminerz.filmagentaproto.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.database.remote.protos.Socio
import com.arnyminerz.filmagentaproto.ui.screens.DateFormatter

@Composable
@ExperimentalMaterial3Api
fun PersonalDataCard(data: Socio) {
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
            stringResource(R.string.profile_title),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall,
            fontSize = 22.sp,
        )
        ReadField(
            value = data.eMail,
            label = R.string.profile_email,
        )
        ReadField(
            value = data.Direccion,
            label = R.string.profile_address,
        )
        ReadField(
            value = data.FecNacimiento?.let { DateFormatter.format(it) },
            label = R.string.profile_birth_date,
        )
        ReadField(
            value = data.Dni,
            label = R.string.profile_nif,
        )
        ReadField(
            value = data.TlfParticular,
            label = R.string.profile_personal_phone,
        )
        ReadField(
            value = data.TlfMovil,
            label = R.string.profile_mobile_phone,
        )
        ReadField(
            value = data.TlfTrabajo,
            label = R.string.profile_work_phone,
        )
        ReadField(
            value = data.FecAlta?.let { DateFormatter.format(it) },
            label = R.string.profile_registration_date,
        )
    }
}