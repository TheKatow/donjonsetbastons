package com.pingbridge.model

data class MessengerApp(
    val packageName: String,
    val displayName: String,
    val iconEmoji: String
) {
    companion object {
        val ALL = listOf(
            MessengerApp("com.whatsapp", "WhatsApp", "\uD83D\uDCAC"),
            MessengerApp("com.facebook.orca", "Messenger", "\uD83D\uDC99"),
            MessengerApp("com.instagram.android", "Instagram", "\uD83D\uDCF7"),
            MessengerApp("com.snapchat.android", "Snapchat", "\uD83D\uDC7B"),
            MessengerApp("com.linkedin.android", "LinkedIn", "\uD83D\uDCBC"),
            MessengerApp("org.thoughtcrime.securesms", "Signal", "\uD83D\uDD12"),
            MessengerApp("org.telegram.messenger", "Telegram", "\u2708\uFE0F"),
            MessengerApp("com.google.android.apps.messaging", "SMS (Google Messages)", "\uD83D\uDCE9"),
            MessengerApp("com.android.mms", "SMS (Stock)", "\uD83D\uDCE9"),
            MessengerApp("com.samsung.android.messaging", "SMS (Samsung)", "\uD83D\uDCE9"),
        )

        fun findByPackage(packageName: String): MessengerApp? =
            ALL.find { it.packageName == packageName }
    }
}
