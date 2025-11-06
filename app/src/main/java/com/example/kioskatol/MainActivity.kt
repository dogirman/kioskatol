package com.example.kioskatol

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.flexbox.FlexboxLayout

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var appGrid: FlexboxLayout
    private lateinit var btnWifiSettings: Button
    private lateinit var btnAdminMode: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appGrid = findViewById(R.id.appGrid)
        btnWifiSettings = findViewById(R.id.btnWifiSettings)
        btnAdminMode = findViewById(R.id.btnAdminMode)

        prefs = getSharedPreferences("kiosk_prefs", MODE_PRIVATE)

        setupAppGrid()
        setupButtons()
        blockStatusBar()
        blockBackButton()
    }

    private fun setupAppGrid() {
        val pm = packageManager
        val allowedApps = prefs.getStringSet("allowed_apps", emptySet()) ?: emptySet()

        val allApps = pm.getInstalledApplications(0)
        val appsToShow = allApps.filter { allowedApps.contains(it.packageName) }

        appGrid.removeAllViews()

        for (app in appsToShow) {
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(24, 24, 24, 24)
            }

            val iconView = ImageView(this).apply {
                setImageDrawable(app.loadIcon(pm))
                layoutParams = LinearLayout.LayoutParams(150, 150)
            }

            val labelView = TextView(this).apply {
                text = app.loadLabel(pm)
                textSize = 14f
                gravity = Gravity.CENTER
                setPadding(0, 8, 0, 0)
            }

            itemLayout.addView(iconView)
            itemLayout.addView(labelView)

            itemLayout.setOnClickListener {
                pm.getLaunchIntentForPackage(app.packageName)?.let { startActivity(it) }
            }

            appGrid.addView(itemLayout)
        }
    }

    private fun setupButtons() {
        // Открытие настроек Wi-Fi
        btnWifiSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
        }

        // Вход в админ-режим
        btnAdminMode.setOnClickListener {
            showAdminPasswordDialog()
        }
    }

    private fun showAdminPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_admin_password, null)
        val passwordInput = dialogView.findViewById<EditText>(R.id.editPassword)

        AlertDialog.Builder(this)
            .setTitle("Вход в админ режим")
            .setView(dialogView)
            .setPositiveButton("Войти") { dialog, _ ->
                val enteredPassword = passwordInput.text.toString()
                val savedPassword = prefs.getString("admin_password", "1234")

                if (enteredPassword == savedPassword) {
                    val intent = Intent(this, AppListActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Неверный пароль", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun blockBackButton() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Блокируем кнопку "Назад" в обычном режиме
            }
        })
    }

    /**
     * Попытка блокировки шторки уведомлений.
     * Работает не на всех устройствах (в зависимости от производителя).
     */
    private fun blockStatusBar() {
        try {
            val statusBarService = Class.forName("android.app.StatusBarManager")
            val service = getSystemService("statusbar")
            val collapse = statusBarService.getMethod("collapsePanels")
            collapse.invoke(service)
        } catch (e: Exception) {
            // Игнорируем, если не поддерживается
        }
    }

    override fun onResume() {
        super.onResume()
        blockStatusBar()
    }
}
