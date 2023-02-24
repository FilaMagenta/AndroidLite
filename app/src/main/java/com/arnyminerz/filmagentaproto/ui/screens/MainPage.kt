package com.arnyminerz.filmagentaproto.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arnyminerz.filmagentaproto.database.data.PersonalData
import com.arnyminerz.filmagentaproto.ui.components.BalanceCard
import com.arnyminerz.filmagentaproto.ui.components.TransactionCard

@Composable
fun MainPage(data: PersonalData) {
    LazyColumn {
        item {
            // Balance
            BalanceCard(
                data.inwards,
                data.outwards,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 8.dp),
            )
        }
        items(data.transactions) { transaction ->
            TransactionCard(transaction)
        }
    }
}