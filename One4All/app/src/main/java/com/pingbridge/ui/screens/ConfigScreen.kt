package com.pingbridge.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pingbridge.data.PreferencesManager
import com.pingbridge.model.MessengerApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen() {
    val context = LocalContext.current
    val prefsManager = remember { PreferencesManager(context) }
    val scope = rememberCoroutineScope()

    val preferredAppPkg by prefsManager.preferredAppPackage.collectAsState(initial = "com.whatsapp")
    val enabledSources by prefsManager.enabledSourceApps.collectAsState(initial = emptySet())

    var preferredAppExpanded by remember { mutableStateOf(false) }

    val preferredApp = MessengerApp.findByPackage(preferredAppPkg)
    val preferredAppList = listOf(
        MessengerApp("com.whatsapp", "WhatsApp", "\uD83D\uDCAC"),
        MessengerApp("com.facebook.orca", "Messenger", "\uD83D\uDC99"),
        MessengerApp("org.telegram.messenger", "Telegram", "\u2708\uFE0F"),
        MessengerApp("org.thoughtcrime.securesms", "Signal", "\uD83D\uDD12"),
        MessengerApp("com.google.android.apps.messaging", "SMS (Google Messages)", "\uD83D\uDCE9"),
    )

    var notificationListenerGranted by remember { mutableStateOf(isNotificationListenerEnabled(context)) }
    var accessibilityGranted by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }

    LaunchedEffect(Unit) {
        while (true) {
            notificationListenerGranted = isNotificationListenerEnabled(context)
            accessibilityGranted = isAccessibilityServiceEnabled(context)
            delay(2000)
        }
    }

    val sourceApps = remember { MessengerApp.ALL }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("One4All", fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Centralisez toutes vos notifications de messagerie",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Les notifications des applications sources sont redirig\u00E9es vers l'application pr\u00E9f\u00E9r\u00E9e. R\u00E9pondez depuis votre app de confiance.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Permissions requises",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    PermissionRow(
                        title = "Notification Listener",
                        description = "Permet d'intercepter les notifications des applications sources",
                        granted = notificationListenerGranted,
                        onRequest = {
                            context.startActivity(
                                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PermissionRow(
                        title = "Service d'Accessibilit\u00E9",
                        description = "Permet d'injecter automatiquement les r\u00E9ponses dans les applications",
                        granted = accessibilityGranted,
                        onRequest = {
                            context.startActivity(
                                Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            )
                        }
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Application pr\u00E9f\u00E9r\u00E9e",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Les notifications seront format\u00E9es avec le style de cette application",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    ExposedDropdownMenuBox(
                        expanded = preferredAppExpanded,
                        onExpandedChange = { preferredAppExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = "${preferredApp?.iconEmoji ?: ""}  ${preferredApp?.displayName ?: "S\u00E9lectionner"}",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = preferredAppExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = { Text("App de r\u00E9ception") }
                        )

                        ExposedDropdownMenu(
                            expanded = preferredAppExpanded,
                            onDismissRequest = { preferredAppExpanded = false }
                        ) {
                            preferredAppList.forEach { app ->
                                DropdownMenuItem(
                                    text = { Text("${app.iconEmoji}  ${app.displayName}") },
                                    onClick = {
                                        scope.launch { prefsManager.setPreferredApp(app.packageName) }
                                        preferredAppExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Applications sources",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cochez les applications dont vous souhaitez centraliser les notifications",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    sourceApps.forEach { app ->
                        val isChecked = enabledSources.contains(app.packageName)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    scope.launch {
                                        prefsManager.toggleSourceApp(app.packageName, checked)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${app.iconEmoji}  ${app.displayName}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Les notifications des applications sources seront redirig\u00E9es vers votre application pr\u00E9f\u00E9r\u00E9e avec le pr\u00E9fixe de l'application d'origine. Utilisez l'action \"R\u00E9pondre\" directement depuis la notification.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    description: String,
    granted: Boolean,
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (granted) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (granted) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (!granted) {
            Spacer(modifier = Modifier.width(8.dp))
            FilledTonalButton(onClick = onRequest) {
                Text("Activer")
            }
        }
    }
}

private fun isNotificationListenerEnabled(context: Context): Boolean {
    val enabledListeners = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    ) ?: return false
    return enabledListeners.contains(context.packageName)
}

private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false
    return enabledServices.contains(context.packageName)
}
