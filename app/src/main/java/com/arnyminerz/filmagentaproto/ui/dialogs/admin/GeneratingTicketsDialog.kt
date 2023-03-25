package com.arnyminerz.filmagentaproto.ui.dialogs.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.work.WorkInfo
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.worker.TicketWorker

@Composable
fun GeneratingTicketsDialog(progress: Pair<Int, Int>, generationState: List<WorkInfo>?) {
    val (current, max) = progress

    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(R.string.admin_tickets_generating)) },
        text = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
                if (max > 0) Text("$current / $max")
                if (generationState?.isNotEmpty() == true) {
                    val workInfo = generationState.first()
                    val progressData = workInfo.progress
                    val workerCurrent = progressData.getInt(TicketWorker.PROGRESS_CURRENT, 0)
                    val workerMax = progressData.getInt(TicketWorker.PROGRESS_MAX, 0)
                    if (workerMax > 0) Text("$workerCurrent / $workerMax")
                }
            }
        },
        confirmButton = {},
    )
}
