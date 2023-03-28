package com.arnyminerz.filmagentaproto.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.WorkInfo
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.activity.MainActivity
import com.arnyminerz.filmagentaproto.database.data.Transaction
import com.arnyminerz.filmagentaproto.database.data.inwards
import com.arnyminerz.filmagentaproto.database.data.outwards
import com.arnyminerz.filmagentaproto.ui.components.BalanceCard
import com.arnyminerz.filmagentaproto.ui.components.TransactionCard
import com.arnyminerz.filmagentaproto.worker.ProgressStep
import com.arnyminerz.filmagentaproto.worker.SyncWorker
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun MainPage(transactions: List<Transaction>, viewModel: MainActivity.MainViewModel) {
    val context = LocalContext.current

    val workerState by viewModel.workerState.observeAsState()
    val isRefreshing by viewModel.isLoading.observeAsState(true)

    val associatedAccounts by viewModel.associatedAccountsTransactions.observeAsState(emptyMap())

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = { SyncWorker.run(context) },
        modifier = Modifier.fillMaxSize(),
    ) {
        val listState = rememberLazyListState()
        LazyColumn(state = listState) {
            items(workerState?.filter { it.state == WorkInfo.State.RUNNING && !it.state.isFinished } ?: emptyList()) { workInfo ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                ) {
                    LaunchedEffect(Unit) {
                        listState.scrollToItem(0)
                    }

                    val progressData = workInfo.progress
                    val step = progressData
                        .getString(SyncWorker.PROGRESS_STEP)
                        ?.let { ProgressStep.valueOf(it) }
                        ?: ProgressStep.INITIALIZING
                    val progress = progressData.getDouble(SyncWorker.PROGRESS, 0.0)

                    Text(
                        text = stringResource(R.string.sync_running),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .padding(top = 8.dp),
                    )
                    Text(
                        text = stringResource(step.textRes),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                    progress.takeIf { it != 0.0 }?.let {
                        LinearProgressIndicator(
                            progress = it.toFloat(),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } ?: LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            // Balance
            item {
                BalanceCard(
                    transactions.inwards,
                    transactions.outwards,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(bottom = 8.dp),
                )
            }
            // Associated accounts balance
            items(associatedAccounts.toList()) { (associatedSocio, transactions) ->
                BalanceCard(
                    transactions.inwards,
                    transactions.outwards,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(bottom = 8.dp),
                    title = "${associatedSocio.Nombre} ${associatedSocio.Apellidos}",
                )
            }
            items(
                transactions.sortedByDescending { it.date.time },
            ) { TransactionCard(it) }
        }
    }
}