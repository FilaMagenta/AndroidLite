package com.arnyminerz.filmagentaproto.ui.screens

import android.accounts.Account
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.account.Authenticator
import com.arnyminerz.filmagentaproto.database.remote.protos.Socio
import com.arnyminerz.filmagentaproto.ui.components.ReadField
import com.arnyminerz.filmagentaproto.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Locale

private val DateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

@Composable
@ExperimentalMaterial3Api
fun ProfilePage(
    data: Socio,
    accounts: Array<out Account>,
    onAccountSelected: (account: Account, index: Int) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        var expanded by remember { mutableStateOf(false) }

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { expanded = !expanded }.menuAnchor(),
            ) {
                Text(
                    text = data.fullName,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .padding(top = 8.dp, start = 24.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 22.sp,
                )
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ArrowDropUp else Icons.Rounded.ArrowDropDown,
                    null,
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                accounts.forEachIndexed { index, account ->
                    DropdownMenuItem(
                        text = { Text(account.name) },
                        onClick = {
                            onAccountSelected(account, index)
                            expanded = false
                        },
                    )
                }
            }
        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    showBackground = true,
    showSystemUi = true,
)
@Composable
fun ProfilePagePreview() {
    AppTheme {
        ProfilePage(
            Socio.EXAMPLE,
            arrayOf(Account("Testing", Authenticator.AuthTokenType)),
        ) { _, _ -> }
    }
}
