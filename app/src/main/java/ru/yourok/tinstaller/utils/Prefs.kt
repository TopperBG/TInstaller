package ru.yourok.tinstaller.utils

import android.preference.PreferenceManager
import ru.yourok.tinstaller.app.App

object Prefs {

    fun getURL(): String {
        return get("catalog_host", "")
        // https://releases.yourok.ru/TI.json
    }

    fun setURL(url: String) {
        set("catalog_host", url)
    }

    fun isLicenseAgree(): Boolean {
        return get("isLicenseAgree", false)
    }

    fun setLicenseAgree() {
        set("isLicenseAgree", true)
    }

    fun <T> get(name: String, def: T): T {
        try {
            val prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext())
            if (prefs.all.containsKey(name))
                return prefs.all[name] as T
            return def
        } catch (e: Exception) {
            return def
        }
    }

    fun set(name: String, value: Any?) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(App.getContext())
        when (value) {
            is String -> prefs.edit().putString(name, value).apply()
            is Boolean -> prefs.edit().putBoolean(name, value).apply()
            is Float -> prefs.edit().putFloat(name, value).apply()
            is Int -> prefs.edit().putInt(name, value).apply()
            is Long -> prefs.edit().putLong(name, value).apply()
            is MutableSet<*>? -> prefs.edit().putStringSet(
                name,
                value as MutableSet<String>?
            ).apply()
            else -> prefs.edit().putString(name, value.toString()).apply()
        }
    }
}