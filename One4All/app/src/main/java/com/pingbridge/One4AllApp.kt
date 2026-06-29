package com.pingbridge

import android.app.Application
import com.pingbridge.util.NotificationHelper

class One4AllApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}
