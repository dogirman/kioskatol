package com.example.kioskatol

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.GridView
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var gridView: GridView
    private lateinit var adminButton: ImageButton
    private lateinit var prefs: SharedPreferences
    private lateinit var pm: PackageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("kiosk_prefs", MODE_PRIVATE)
        pm = packageManager

        gridView = findViewById(R.id.appGrid)
        adminButton = findViewById(R.id.btnAdmin)

        val allowedApps = prefs.getStringSet("allowed_apps", emptySet()) ?: emptySet()
        val appList = allowedApps.mapNotNull { pkg ->
            try { pm.getApplicationInfo(pkg, 0) } catch (e: Exception) { null }
        }

        val adapter = object : android.widget.BaseAdapter() {
            override fun getCount() = appList.size
            override fun getItem(pos: Int) = appList[pos]
            override fun getItemId(pos: Int) = pos.toLong()
            override fun getView(pos: Int, convertView: android.view.View?, parent: android.view.ViewGroup?): android.view.View {
                val view = layoutInflater.inflate(R.layout.item_app_icon, parent, false)
                val appInfo = appList[pos]
                val icon = view.findViewById<android.widget.ImageView>(R.id.appIcon)
                val label = view.findViewById<android.widget.TextView>(R.id.appLabel)
                icon.setImageDrawable(pm.getApplicationIcon(appInfo))
                label.text = pm.getApplicationLabel(appInfo)
                view.setOnClickListener {
                    val launchIntent = pm.getLaunchIntentForPackage(appInfo.packageName)
                    if (launchIntent != null) startActivity(launchIntent)
                    else Toast.makeText(this@MainActivity, "Невозможно открыть", Toast.LENGTH_SHORT).show()
                }
                return view
            }
        }
        gridView.adapter = adapter

        adminButton.setOnClickListener {
            startActivity(Intent(this, AdminLoginActivity::class.java))
        }
    }

    override fun onBackPressed() {
        // Блокируем кнопку "Назад"
    }
}
