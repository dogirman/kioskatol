package com.example.kioskatol.data

import android.content.Context

data class AppEntry(val packageName: String, val label: String)

class Prefs(context: Context) {
    private val prefs = context.getSharedPreferences("kiosk_prefs", Context.MODE_PRIVATE)

    fun setAppAllowed(pkg: String, allowed: Boolean, label: String) {
        prefs.edit().putString(pkg, if (allowed) label else null).apply()
    }

    fun isAppAllowed(pkg: String) = prefs.contains(pkg)

    fun getAllowedApps(): List<AppEntry> =
        prefs.all.map { AppEntry(it.key, it.value.toString()) }
}
