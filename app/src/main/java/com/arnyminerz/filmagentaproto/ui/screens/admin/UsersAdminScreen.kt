package com.arnyminerz.filmagentaproto.ui.screens.admin

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.database.data.woo.Customer

@Composable
fun UsersAdminScreen(customers: List<Customer>?) {
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Text(
                text = stringResource(R.string.admin_users_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 26.sp,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.admin_users_message),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            )
        }
        items(customers ?: emptyList()) { customer ->
            Text(customer.fullName)
        }
    }
}
