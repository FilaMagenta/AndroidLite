package com.arnyminerz.filmagentaproto.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.arnyminerz.filmagentaproto.SyncWorker
import com.arnyminerz.filmagentaproto.activity.MainActivity
import com.arnyminerz.filmagentaproto.database.data.PersonalData
import com.arnyminerz.filmagentaproto.ui.components.BalanceCard
import com.arnyminerz.filmagentaproto.ui.components.TransactionCard
import com.arnyminerz.filmagentaproto.ui.components.UnacceptedPolicyCard
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun MainPage(data: PersonalData, viewModel: MainActivity.MainViewModel) {
    val context = LocalContext.current
    val isRefreshing by viewModel.isLoading.observeAsState(true)

    val associatedAccounts by viewModel.associatedAccounts.observeAsState(emptyList())

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = { SyncWorker.run(context) },
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn {
            // Balance
            item {
                BalanceCard(
                    data.inwards,
                    data.outwards,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(bottom = 8.dp),
                )
            }
            // Associated accounts balance
            items(associatedAccounts) { (associatedSocio, associatedData) ->
                if (associatedData == null)
                    UnacceptedPolicyCard(
                        associatedSocio,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 8.dp),
                    )
                else
                    BalanceCard(
                        associatedData.inwards,
                        associatedData.outwards,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 8.dp),
                        title = "${associatedSocio.Nombre} ${associatedSocio.Apellidos}",
                    )
            }
            items(data.transactions) { transaction ->
                TransactionCard(transaction)
            }
        }
    }
}