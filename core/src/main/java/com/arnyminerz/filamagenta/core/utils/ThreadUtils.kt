package com.arnyminerz.filamagenta.core.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun doAsync(block: suspend CoroutineScope.() -> Unit): Job = CoroutineScope(Dispatchers.Main).launch {
    withContext(Dispatchers.IO, block = block)
}

/**
 * Runs the given [block] of code in the IO thread.
 * @return The value returned by [block]
 */
suspend fun <R> io(block: suspend CoroutineScope.() -> R): R =
    withContext(Dispatchers.IO, block)

/**
 * Runs the given [block] of code in the main thread (UI thread).
 * @return The value returned by [block]
 */
suspend fun <R> ui(block: suspend CoroutineScope.() -> R): R =
    withContext(Dispatchers.Default, block)
