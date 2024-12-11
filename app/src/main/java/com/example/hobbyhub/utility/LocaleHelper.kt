package com.example.hobbyhub.utility

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import java.util.*

object LocaleHelper {
    /**
     * Set the app's locale to the specified language.
     * @param context Application or Activity context.
     * @param language The language code, e.g., "en", "fr".
     * @return The updated context with the new locale applied.
     */
    fun setLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = context.resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        // Log configuration details
        Log.d("LocaleHelper", "Locale set to: ${config.locales[0]}")

        return context.createConfigurationContext(config)
    }

    /**
     * Get the current locale language code.
     * @param context Application or Activity context.
     * @return The language code, e.g., "en", "fr".
     */
    fun getCurrentLanguage(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0].language
        } else {
            context.resources.configuration.locale.language
        }
    }
}