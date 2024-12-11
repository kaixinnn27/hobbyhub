package com.example.hobbyhub.utility

import android.content.Context
import android.content.SharedPreferences

object LocaleManager {
    private const val PREFS_NAME = "user_preferences"
    private const val KEY_LOCALE = "locale"

    fun setLocale(context: Context, locale: String) {
        // Save locale to SharedPreferences
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(KEY_LOCALE, locale)
        editor.apply()

        // Apply the locale to the app
        LocaleHelper.setLocale(context, locale)
    }

    fun getLocale(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LOCALE, "en") ?: "en"  // Default to English if not set
    }
}