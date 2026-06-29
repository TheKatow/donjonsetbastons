# Keep DataStore serializers
-keepclassmembers class * {
    @androidx.datastore.preferences.core.PreferencesDelegate *;
}

# Keep service classes
-keep class com.pingbridge.service.** { *; }
-keep class com.pingbridge.reply.** { *; }
