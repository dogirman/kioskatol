package com.example.kioskatol

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.content.Intent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import android.app.ActivityManager
import android.os.Handler
import android.os.Looper


class ChromeBlockerService : AccessibilityService() {

    private val allowedDomain = "whatsapp.com"
    private var wasDownloading = false
    private var watchdogHandler = Handler(Looper.getMainLooper())
    private var watchdogRunnable: Runnable? = null

    override fun onServiceConnected() {
        startWatchdog()
    }
    private fun startWatchdog() {
        watchdogRunnable = object : Runnable {
            override fun run() {
                enforceKiosk()
                watchdogHandler.postDelayed(this, 300) // каждые 300 мс
            }
        }
        watchdogHandler.post(watchdogRunnable!!)
    }


    private fun enforceKiosk() {
        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val tasks = am.appTasks

        if (tasks.isNullOrEmpty()) return

        val top = tasks[0].taskInfo.topActivity ?: return
        val pkg = top.packageName ?: return

        // Если запущен Chrome — убить
        if (pkg == "com.android.chrome") {
            am.killBackgroundProcesses("com.android.chrome")
            returnToKiosk()
        }
    }

    private fun returnToKiosk() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        )
        startActivity(intent)
    }



    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkg = event.packageName?.toString() ?: return

        if (pkg == "com.android.chrome") {
            closeChrome()
            returnToKiosk()
        }
    }



    private fun containsText(root: AccessibilityNodeInfo, text: String): Boolean {
        return root.findAccessibilityNodeInfosByText(text).isNotEmpty()
    }

    private fun containsAnyText(root: AccessibilityNodeInfo, list: List<String>): Boolean {
        return list.any { containsText(root, it) }
    }



    private fun killChrome() {
        try {
            val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            am.killBackgroundProcesses("com.android.chrome")
        } catch (_: Exception) {}
    }



    private fun closeChrome() {
        try {
            val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            am.killBackgroundProcesses("com.android.chrome")
        } catch (_: Exception) {}
    }


    override fun onInterrupt() {}
}
