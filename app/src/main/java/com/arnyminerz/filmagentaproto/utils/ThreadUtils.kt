package com.arnyminerz.filmagentaproto.utils

import androidx.annotation.WorkerThread
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arnyminerz.filamagenta.core.utils.io
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

@Composable
fun <T : Any, R : Any> LaunchedEffectFlow(
    obj: T,
    property: (obj: T) -> R,
    @WorkerThread block: suspend (value: R) -> Unit
) {
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

suspend fun <T> LiveData<T>.await(): T = withContext(Dispatchers.Main.immediate) {
    suspendCancellableCoroutine { continuation ->
        val observer = object : Observer<T> {
            override fun onChanged(value: T) {
                removeObserver(this)
                continuation.resume(value)
            }
        }

        observeForever(observer)

        continuation.invokeOnCancellation {
            removeObserver(observer)
        }
    }
}
