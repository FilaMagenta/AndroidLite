package com.arnyminerz.filmagentaproto.utils

import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking

/**
 * Blocks the current thread until the [Job] is completed.
 */
fun Job.await() {
    runBlocking {
        suspendCoroutine<Void?> { cont ->
            invokeOnCompletion { cont.resume(null) }
        }
    }
}
