package com.pingbridge.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.pingbridge.R
import com.pingbridge.reply.ReplyReceiver

object NotificationHelper {

    val CHANNEL_FORWARDED = "forwarded_notifications"
    val REPLY_ACTION_KEY = "key_reply_text"
    val EXTRA_TARGET_PACKAGE = "extra_target_package"
    val EXTRA_SENDER = "extra_sender"
    val EXTRA_SOURCE_APP = "extra_source_app"
    val EXTRA_MESSAGE = "extra_message"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val forwardedChannel = NotificationChannel(
            CHANNEL_FORWARDED,
            "Notifications centralis\u00E9es",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications transf\u00E9r\u00E9es depuis les applications sources"
        }

        manager.createNotificationChannel(forwardedChannel)
    }

    fun postForwardedNotification(
        context: Context,
        notificationId: Int,
        sourceAppLabel: String,
        sourcePackage: String,
        senderName: String,
        message: String,
        preferredAppColor: Int
    ) {
        val replyIntent = Intent(context, ReplyReceiver::class.java).apply {
            putExtra(EXTRA_TARGET_PACKAGE, sourcePackage)
            putExtra(EXTRA_SENDER, senderName)
            putExtra(EXTRA_SOURCE_APP, sourceAppLabel)
            putExtra(EXTRA_MESSAGE, message)
        }

        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val remoteInput = RemoteInput.Builder(REPLY_ACTION_KEY).build()

        val replyAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_send,
            "R\u00E9pondre",
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()

        val title = "[$sourceAppLabel] $senderName"

        val notification = NotificationCompat.Builder(context, CHANNEL_FORWARDED)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setColor(preferredAppColor)
            .setAutoCancel(true)
            .addAction(replyAction)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification.build())
    }

    fun getReplyText(intent: Intent): CharSequence? {
        return RemoteInput.getResultsFromIntent(intent)?.getCharSequence(REPLY_ACTION_KEY)
    }
}
