package com.arnyminerz.filmagentaproto.ui.screens

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
import com.arnyminerz.filmagentaproto.activity.MainActivity
import com.arnyminerz.filmagentaproto.SyncWorker
import com.arnyminerz.filmagentaproto.database.data.PersonalData
import com.arnyminerz.filmagentaproto.ui.components.BalanceCard
import com.arnyminerz.filmagentaproto.ui.components.TransactionCard
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun MainPage(data: PersonalData, viewModel: MainActivity.MainViewModel) {
    val context = LocalContext.current
    val isRefreshing by viewModel.isLoading.observeAsState(true)

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = { SyncWorker.run(context) },
    ) {
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
}