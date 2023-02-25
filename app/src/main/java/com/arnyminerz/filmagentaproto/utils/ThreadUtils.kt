package com.arnyminerz.filmagentaproto.utils

import androidx.annotation.WorkerThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun doAsync(@WorkerThread block: suspend CoroutineScope.() -> Unit): Job = CoroutineScope(Dispatchers.IO).launch(block = block)

@Composable
fun <T: Any, R: Any> LaunchedEffectFlow(obj: T, property: (obj: T) -> R, @WorkerThread block: suspend (value: R) -> Unit) {
    LaunchedEffect(obj) {
        snapshotFlow { property(obj) }.collect { block(it) }
    }
}
