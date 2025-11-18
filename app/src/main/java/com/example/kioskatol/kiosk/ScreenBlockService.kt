package com.example.kioskatol.kiosk

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.WindowManager
import android.widget.Toast
import android.view.Gravity
import android.view.View

class ScreenBlockService : AccessibilityService() {

    private var overlayView: View? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        Toast.makeText(this, "Kiosk блокировка активна", Toast.LENGTH_SHORT).show()

        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            80, // высота верхнего экрана для перехвата шторки
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            android.graphics.PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP

        overlayView = View(this)
        overlayView!!.setBackgroundColor(0x00000000)
        wm.addView(overlayView, params)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // ничего не делаем
    }

    override fun onInterrupt() {
        // ничего не делаем
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let {
            val wm = getSystemService(WINDOW_SERVICE) as WindowManager
            wm.removeView(it)
        }
    }
}
