package com.pingbridge.service

import android.app.Notification
import android.content.ComponentName
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import com.pingbridge.data.PreferencesManager
import com.pingbridge.model.MessengerApp
import com.pingbridge.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class BridgeNotificationListenerService : NotificationListenerService() {

    private lateinit var prefsManager: PreferencesManager
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        prefsManager = PreferencesManager(this)
        NotificationHelper.createChannels(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        if (!isSourceAppEnabled(packageName)) return

        val notification = sbn.notification
        val extras = notification.extras ?: return

        val title = extras.getString(Notification.EXTRA_TITLE, "")
        val text = extras.getString(Notification.EXTRA_TEXT, "")
        val ticker = extras.getString(Notification.EXTRA_SUB_TEXT, "")

        val sender = title.ifBlank { ticker }.ifBlank { "Inconnu" }
        val message = text.ifBlank { "(pi\u00E8ce jointe)" }

        val appInfo = MessengerApp.findByPackage(packageName)
        val appLabel = appInfo?.displayName ?: packageName

        val isGroup = extras.getString(NotificationCompat.EXTRA_SUMMARY_TEXT) != null
        if (isGroup || sender.isBlank()) return

        val preferredColor = getPreferredAppColor()

        scope.launch {
            NotificationHelper.postForwardedNotification(
                context = this@BridgeNotificationListenerService,
                notificationId = sbn.id,
                sourceAppLabel = appLabel,
                sourcePackage = packageName,
                senderName = sender,
                message = message,
                preferredAppColor = preferredColor
            )
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            requestRebind(ComponentName(this, javaClass))
        }
    }

    private fun isSourceAppEnabled(packageName: String): Boolean {
        val enabledSet = runBlocking {
            prefsManager.enabledSourceApps.first()
        }
        return enabledSet.contains(packageName)
    }

    private fun getPreferredAppColor(): Int {
        val preferredPkg = runBlocking {
            prefsManager.preferredAppPackage.first()
        }
        return when {
            preferredPkg.contains("whatsapp") -> 0xFF25D366.toInt()
            preferredPkg.contains("facebook") || preferredPkg.contains("messenger") -> 0xFF0084FF.toInt()
            preferredPkg.contains("instagram") -> 0xFFE1306C.toInt()
            preferredPkg.contains("snapchat") -> 0xFFFFFC00.toInt()
            preferredPkg.contains("linkedin") -> 0xFF0077B5.toInt()
            preferredPkg.contains("telegram") -> 0xFF0088CC.toInt()
            preferredPkg.contains("signal") -> 0xFF3A76F0.toInt()
            else -> 0xFF2196F3.toInt()
        }
    }
}
