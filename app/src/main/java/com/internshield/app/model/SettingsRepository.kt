package com.internshield.app.model

import android.content.Context
import android.content.SharedPreferences

object SettingsRepository {
    private const val PREFS_NAME = "internshield_settings"
    
    private const val KEY_PROTECTION_ENABLED = "protection_enabled"
    private const val KEY_AI_BACKEND_ENABLED = "ai_backend_enabled"
    private const val KEY_BACKEND_URL = "backend_url"
    private const val KEY_GEMINI_API_KEY = "gemini_api_key"
    private const val KEY_SCAN_UNKNOWN_ONLY = "scan_unknown_only"
    private const val KEY_REALTIME_ALERTS = "realtime_alerts"
    private const val KEY_RUN_IN_BACKGROUND = "run_in_background"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isProtectionEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_PROTECTION_ENABLED, true)
    }

    fun setProtectionEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_PROTECTION_ENABLED, enabled).apply()
    }

    fun isAiBackendEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AI_BACKEND_ENABLED, true)
    }

    fun setAiBackendEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AI_BACKEND_ENABLED, enabled).apply()
    }

    fun getBackendUrl(context: Context): String {
        return getPrefs(context).getString(KEY_BACKEND_URL, "http://10.0.2.2:3000/api/analyze") ?: "http://10.0.2.2:3000/api/analyze"
    }

    fun setBackendUrl(context: Context, url: String) {
        getPrefs(context).edit().putString(KEY_BACKEND_URL, url).apply()
    }

    fun getGeminiApiKey(context: Context): String {
        return getPrefs(context).getString(KEY_GEMINI_API_KEY, "") ?: ""
    }

    fun setGeminiApiKey(context: Context, key: String) {
        getPrefs(context).edit().putString(KEY_GEMINI_API_KEY, key).apply()
    }

    fun isScanUnknownSendersOnly(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SCAN_UNKNOWN_ONLY, true)
    }

    fun setScanUnknownSendersOnly(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SCAN_UNKNOWN_ONLY, enabled).apply()
    }

    fun isRealTimeAlertsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_REALTIME_ALERTS, true)
    }

    fun setRealTimeAlertsEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_REALTIME_ALERTS, enabled).apply()
    }

    fun isRunInBackgroundEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_RUN_IN_BACKGROUND, true)
    }

    fun setRunInBackgroundEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_RUN_IN_BACKGROUND, enabled).apply()
    }
}
