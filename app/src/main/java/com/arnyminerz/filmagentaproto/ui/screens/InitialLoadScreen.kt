package com.arnyminerz.filmagentaproto.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.worker.ProgressStep
import com.arnyminerz.filmagentaproto.worker.SyncWorker
import timber.log.Timber

@Composable
fun InitialLoadScreen() {
    val context = LocalContext.current
    val state by SyncWorker.getLiveStates(context).observeAsState()

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        state
            ?.firstOrNull { !it.tags.contains(SyncWorker.TAG_PERIODIC) && it.state == WorkInfo.State.RUNNING }
            ?.let { workInfo ->
                // Worker is running
                Card(
                    Modifier
                        .sizeIn(maxWidth = 400.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.first_load_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .padding(top = 8.dp, bottom = 4.dp),
                    )
                    Text(
                        text = stringResource(R.string.first_load_message),
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 8.dp),
                    )
                    val (step, progress) = workInfo.progress
                        .let { progressData ->
                            val step = progressData.getString(SyncWorker.PROGRESS_STEP)
                                ?.let { ProgressStep.valueOf(it) }
                            val progress = progressData.getDouble(SyncWorker.PROGRESS, -1.0)
                            step to progress
                        }
                        .takeIf { it.first != null }
                        ?: (ProgressStep.INITIALIZING to -1.0)
                    Text(
                        text = step?.textRes?.let { stringResource(it) } ?: "",
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(8.dp),
                    )
                    if (progress >= 0.0)
                        LinearProgressIndicator(
                            progress = progress.toFloat(),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    else
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                        )
                }
            }
            ?: run {
                // Worker not running
                Card(
                    Modifier
                        .sizeIn(maxWidth = 400.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.first_load_failed_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .padding(top = 8.dp, bottom = 4.dp),
                    )
                    Text(
                        text = stringResource(R.string.first_load_failed_message),
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 8.dp),
                    )
                    OutlinedButton(
                        onClick = {
                            Timber.d("Starting SyncWorker...")
                            val uuid = SyncWorker.run(context)
                            val workerState = SyncWorker.getLiveState(context, uuid)
                            Timber.d("Adding SyncWorker state listeners...")
                            val observer = object : Observer<WorkInfo> {
                                override fun onChanged(value: WorkInfo) {
                                    if (value.state.isFinished) {
                                        Timber.i("Synchronization operation finished. State: ${value.state}")
                                        workerState.removeObserver(this)
                                    }
                                }
                            }
                            workerState.observeForever(observer)
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(end = 8.dp),
                    ) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
    }
}
