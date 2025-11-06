package com.example.kioskatol

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.flexbox.FlexboxLayout

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var appGrid: LinearLayout
    private lateinit var btnWifiSettings: Button
    private lateinit var btnAdminMode: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val appGrid = findViewById<FlexboxLayout>(R.id.appGrid)

        prefs = getSharedPreferences("kiosk_prefs", MODE_PRIVATE)
        btnWifiSettings = findViewById(R.id.btnWifiSettings)
        btnAdminMode = findViewById(R.id.btnAdminMode)

        val allowedApps = prefs.getStringSet("allowed_apps", emptySet()) ?: emptySet()
        val pm = packageManager
        val apps = pm.getInstalledApplications(0)
            .filter { app -> allowedApps.contains(app.packageName) }

        val inflater = LayoutInflater.from(this)
        appGrid.removeAllViews()

        for (app in apps) {
            val iconView = inflater.inflate(R.layout.app_icon_item, appGrid, false) as ImageView
            iconView.setImageDrawable(app.loadIcon(pm))
            iconView.setOnClickListener {
                val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                launchIntent?.let { startActivity(it) }
            }
            appGrid.addView(iconView)
        }

        // Кнопка Wi-Fi
        btnWifiSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)
        }

        // Вход в админ режим с паролем
        btnAdminMode.setOnClickListener {
            showAdminPasswordDialog()
        }

        // Блокировка кнопки "назад"
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Блокируем выход из киоска
            }
        })
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
}
