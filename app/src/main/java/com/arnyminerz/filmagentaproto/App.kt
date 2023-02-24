package com.arnyminerz.filmagentaproto

import android.app.Application

class App: Application() {
    override fun onCreate() {
        super.onCreate()

        SyncWorker.schedule(this)
    }
}