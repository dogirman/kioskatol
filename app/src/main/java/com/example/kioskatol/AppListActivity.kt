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
        val apps = pm.getInstalledApplications(0)
            .filter { app ->
                (pm.getLaunchIntentForPackage(app.packageName) != null &&
                        !app.packageName.startsWith("com.android")) ||
                        app.packageName.contains("settings", true)
            }

        val allowedSet = prefs.getStringSet("allowed_apps", mutableSetOf())?.toMutableSet()
            ?: mutableSetOf()

        adapter = AppListAdapter(this, apps, allowedSet)
        listView.adapter = adapter

        saveButton.setOnClickListener {
            val selected = adapter.getSelectedPackages()
            prefs.edit().putStringSet("allowed_apps", selected).apply()
            Toast.makeText(this, "Сохранено ${selected.size} приложений", Toast.LENGTH_SHORT).show()
        }

        exitButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
