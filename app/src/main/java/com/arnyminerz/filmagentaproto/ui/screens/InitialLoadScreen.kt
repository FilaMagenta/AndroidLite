package com.arnyminerz.filmagentaproto.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.filmagentaproto.ProgressStep
import com.arnyminerz.filmagentaproto.R
import com.arnyminerz.filmagentaproto.SyncWorker

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun InitialLoadScreen() {
    val context = LocalContext.current
    val state by SyncWorker.getLiveState(context).observeAsState()

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
            val (step, progress) = state?.firstOrNull()?.progress?.let { progressData ->
                val step = progressData.getString(SyncWorker.PROGRESS_STEP)
                    ?.let { ProgressStep.valueOf(it) }
                val progress = progressData.getDouble(SyncWorker.PROGRESS, -1.0)
                step to progress
            } ?: (ProgressStep.INITIALIZING to -1.0)
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
}
