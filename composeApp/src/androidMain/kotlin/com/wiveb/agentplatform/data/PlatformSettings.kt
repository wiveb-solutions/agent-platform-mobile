package com.wiveb.agentplatform.data

import android.content.Context
import android.content.SharedPreferences

actual class PlatformSettings(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("agent_platform_prefs", Context.MODE_PRIVATE)

    actual fun getString(key: String, default: String): String {
        return prefs.getString(key, default) ?: default
    }

    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
}
