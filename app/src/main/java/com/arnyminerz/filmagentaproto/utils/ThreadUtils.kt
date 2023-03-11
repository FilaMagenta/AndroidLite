package com.arnyminerz.filmagentaproto.utils

import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun doAsync(@WorkerThread block: suspend CoroutineScope.() -> Unit): Job = CoroutineScope(Dispatchers.Main).launch {
    withContext(Dispatchers.IO, block = block)
}

@Composable
fun <T: Any, R: Any> LaunchedEffectFlow(obj: T, property: (obj: T) -> R, @WorkerThread block: suspend (value: R) -> Unit) {
    LaunchedEffect(obj) {
        snapshotFlow { property(obj) }.collect { block(it) }
    }
}

/**
 * Uses the view model scope to launch the given [block] of code asynchronously.
 * @return A [Job] that observes the completion state of [block]
 * @see viewModelScope
 */
fun ViewModel.async(@WorkerThread block: suspend CoroutineScope.() -> Unit) =
    viewModelScope.launch { io(block) }

/**
 * Runs the given [block] of code in the IO thread.
 * @return The value returned by [block]
 */
suspend fun <R> io(@WorkerThread block: suspend CoroutineScope.() -> R): R =
    withContext(Dispatchers.IO, block)

/**
 * Runs the given [block] of code in the main thread (UI thread).
 * @return The value returned by [block]
 */
suspend fun <R> ui(@UiThread block: suspend CoroutineScope.() -> R): R =
    withContext(Dispatchers.Main, block)
