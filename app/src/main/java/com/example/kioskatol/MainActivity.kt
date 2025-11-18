package com.example.kioskatol

import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.flexbox.FlexboxLayout
import android.net.Uri
import android.view.ViewGroup


class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var appGrid: FlexboxLayout
    private lateinit var btnWifiSettings: Button
    private lateinit var btnAdminMode: Button
    private lateinit var dpm: DevicePolicyManager
    private lateinit var adminComponent: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("kiosk_prefs", MODE_PRIVATE)
        appGrid = findViewById(R.id.appGrid)
        btnWifiSettings = findViewById(R.id.btnWifiSettings)
        btnAdminMode = findViewById(R.id.btnAdminMode)

        dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, DeviceAdminReceiver::class.java)

        // Разрешаем запуск выбранных приложений в киоск-режиме
        allowSelectedAppsForLockTask()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startLockTask()
        }

        setupAppGrid()
        setupButtons()
        blockStatusBar()
        blockBackButton()
    }

    private fun allowSelectedAppsForLockTask() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            val dpm = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val component = ComponentName(this, DeviceAdminReceiver::class.java)

            dpm.setLockTaskPackages(
                component,
                arrayOf(
                    packageName,
                    "com.whatsapp",
                    "com.android.vending",
                    "com.google.android.gms",
                    "com.android.chrome"
                )
            )

            startLockTask()
        }

    }


    private fun setupAppGrid() {
        val pm = packageManager
        val allowedApps = prefs.getStringSet("allowed_apps", emptySet()) ?: emptySet()

        val allApps = pm.getInstalledApplications(0)
        // Отбираем только те приложения, которые в allowed_apps
        val appsToShow = allApps.filter { allowedApps.contains(it.packageName) }

        appGrid.removeAllViews()

        val inflater = LayoutInflater.from(this)

        for (app in appsToShow) {
            // 1) Скрываем Play Market из сетки (но он остаётся в whitelist)
            if (app.packageName == "com.android.vending") {
                continue
            }

            // Контейнер иконки + подписи
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(24, 24, 24, 24)
                layoutParams = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    // Немного отступов между элементами
                    (this as? ViewGroup.MarginLayoutParams)?.setMargins(12, 12, 12, 12)
                }
            }

            // Иконка приложения
            val iconView = ImageView(this).apply {
                setImageDrawable(app.loadIcon(pm))
                layoutParams = LinearLayout.LayoutParams(150, 150)
                isClickable = true
                isFocusable = true
            }

            // Подпись под иконкой
            val labelView = TextView(this).apply {
                text = app.loadLabel(pm)
                textSize = 12f
                gravity = Gravity.CENTER
                setPadding(0, 8, 0, 0)
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
            }

            itemLayout.addView(iconView)
            itemLayout.addView(labelView)

            // Клик по элементу
            itemLayout.setOnClickListener {
                try {
                    // Запускаем приложение напрямую
                    pm.getLaunchIntentForPackage(app.packageName)?.let { launch ->
                        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(launch)
                    }
                } catch (e: Exception) {
                    // fallback: если приложение само не открылось
                    try {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("market://details?id=${app.packageName}")
                            setPackage("com.android.vending")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(intent)
                    } catch (_: Exception) { /* игнор */ }
                }
            }


            appGrid.addView(itemLayout)
        }
    }


    private fun setupButtons() {
        btnWifiSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
        }

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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) stopLockTask()
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
            override fun handleOnBackPressed() {}
        })
    }

    private fun blockStatusBar() {
        try {
            val statusBarService = Class.forName("android.app.StatusBarManager")
            val service = getSystemService("statusbar")
            val collapse = statusBarService.getMethod("collapsePanels")
            collapse.invoke(service)
        } catch (_: Exception) { }
    }

    private fun safeStartActivity(intent: Intent) {

        val url = intent.dataString ?: ""

        // 1 — WhatsApp пытается открыть страницу обновления → принудительно ведём в Google Play
        if (url.contains("whatsapp.com") || url.contains("whatsapp.net")) {
            val marketIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=com.whatsapp")
                setPackage("com.android.vending")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(marketIntent)
            return
        }

        // 2 — всё остальное запускаем как обычно
        startActivity(intent)
    }



    override fun onResume() {
        super.onResume()
        blockStatusBar()
    }
}
