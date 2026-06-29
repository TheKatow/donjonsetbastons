package com.pingbridge.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "one4all_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        private val KEY_PREFERRED_APP = stringPreferencesKey("preferred_app_package")
        private val KEY_SOURCE_APPS = stringSetPreferencesKey("source_apps_enabled")
        private val KEY_PENDING_REPLY_TARGET = stringPreferencesKey("pending_reply_target")
        private val KEY_PENDING_REPLY_TEXT = stringPreferencesKey("pending_reply_text")
    }

    val preferredAppPackage: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_PREFERRED_APP] ?: "com.whatsapp"
    }

    val enabledSourceApps: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_SOURCE_APPS] ?: emptySet()
    }

    val pendingReplyTarget: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_PENDING_REPLY_TARGET]
    }

    val pendingReplyText: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_PENDING_REPLY_TEXT]
    }

    suspend fun setPreferredApp(packageName: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PREFERRED_APP] = packageName
        }
    }

    suspend fun toggleSourceApp(packageName: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_SOURCE_APPS]?.toMutableSet() ?: mutableSetOf()
            if (enabled) current.add(packageName) else current.remove(packageName)
            prefs[KEY_SOURCE_APPS] = current
        }
    }

    suspend fun setPendingReply(targetPackage: String, replyText: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PENDING_REPLY_TARGET] = targetPackage
            prefs[KEY_PENDING_REPLY_TEXT] = replyText
        }
    }

    suspend fun clearPendingReply() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_PENDING_REPLY_TARGET)
            prefs.remove(KEY_PENDING_REPLY_TEXT)
        }
    }

    suspend fun getPendingReplyTarget(): String? {
        return context.dataStore.data.first()[KEY_PENDING_REPLY_TARGET]
    }

    suspend fun getPendingReplyText(): String? {
        return context.dataStore.data.first()[KEY_PENDING_REPLY_TEXT]
    }

    suspend fun isSourceAppEnabled(packageName: String): Boolean {
        return context.dataStore.data.first()[KEY_SOURCE_APPS]?.contains(packageName) ?: false
    }
}
