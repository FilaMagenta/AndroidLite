package com.arnyminerz.filmagentaproto.monitoring

/**
 * **NOT AN ERROR**. Used for sending performance reports through Bugsnag.
 */
class PerformanceNotification(process: String, time: Long): Throwable("Process $process took $time millis.")
