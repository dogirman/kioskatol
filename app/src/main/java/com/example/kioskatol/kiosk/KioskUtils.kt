package com.example.kioskatol.kiosk

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context

object KioskUtils {
    fun enableLockTask(context: Context) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val compName = ComponentName(context, AdminReceiver::class.java)
        if (dpm.isDeviceOwnerApp(context.packageName)) {
            dpm.setLockTaskPackages(compName, arrayOf(context.packageName))
        }
    }
}
