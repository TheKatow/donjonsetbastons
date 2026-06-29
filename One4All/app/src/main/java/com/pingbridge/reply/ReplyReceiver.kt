package com.pingbridge.reply

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pingbridge.util.NotificationHelper
import com.pingbridge.data.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReplyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val replyText = NotificationHelper.getReplyText(intent)
        val targetPackage = intent.getStringExtra(NotificationHelper.EXTRA_TARGET_PACKAGE)
        val sender = intent.getStringExtra(NotificationHelper.EXTRA_SENDER) ?: "Inconnu"

        if (replyText.isNullOrBlank() || targetPackage.isNullOrBlank()) return

        val prefsManager = PreferencesManager(context)

        CoroutineScope(Dispatchers.IO).launch {
            prefsManager.setPendingReply(targetPackage, replyText.toString())

            val launchIntent = context.packageManager.getLaunchIntentForPackage(targetPackage)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(launchIntent)
            }
        }
    }
}
