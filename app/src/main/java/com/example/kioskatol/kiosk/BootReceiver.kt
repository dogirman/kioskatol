package com.example.kioskatol.kiosk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.kioskatol.MainActivity // ✅ важно импортировать MainActivity

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            context?.let {
                val launchIntent = Intent(it, MainActivity::class.java)
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                it.startActivity(launchIntent)
            }
        }
    }
}
