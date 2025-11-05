package com.example.kioskatol

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AppListActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var saveButton: Button
    private lateinit var exitButton: Button
    private lateinit var adapter: AppListAdapter
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_list)

        prefs = getSharedPreferences("kiosk_prefs", MODE_PRIVATE)
        listView = findViewById(R.id.appListView)
        saveButton = findViewById(R.id.btnSave)
        exitButton = findViewById(R.id.btnExitAdmin)

        val pm = packageManager

        val wifiButton = findViewById<Button>(R.id.btnWifiSettings)
        wifiButton.setOnClickListener {
            try {
                startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS))
            } catch (e: Exception) {
                Toast.makeText(this, "Не удалось открыть настройки Wi-Fi", Toast.LENGTH_SHORT).show()
            }
        }


        // получаем список приложений как MutableList
        val apps = pm.getInstalledApplications(0)
            .filter { app ->
                (pm.getLaunchIntentForPackage(app.packageName) != null &&
                        !app.packageName.startsWith("com.android")) ||
                        app.packageName.contains("settings", true)
            }
            .toMutableList() // <- обязательно mutable

        // получаем и приводим к MutableSet (если пусто — новый mutableSet)
        val allowedSet: MutableSet<String> =
            prefs.getStringSet("allowed_apps", null)?.toMutableSet() ?: mutableSetOf()

        // создаём адаптер: (context, packageManager, appList, selectedPackages)
        adapter = AppListAdapter(this, pm, apps, allowedSet)
        listView.adapter = adapter

        saveButton.setOnClickListener {
            val selected = adapter.getSelectedPackages()
            prefs.edit().putStringSet("allowed_apps", selected.toSet()).apply()
            Toast.makeText(this, "Сохранено ${selected.size} приложений", Toast.LENGTH_SHORT).show()
        }

        exitButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    // На случай, если пользователь уходит с экрана — автоматически сохраняем
    override fun onPause() {
        super.onPause()
        // сохраняем текущее состояние выбранных пакетов
        val selected = adapter.getSelectedPackages()
        prefs.edit().putStringSet("allowed_apps", selected.toSet()).apply()
    }
}
