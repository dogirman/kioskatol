package com.example.kioskatol

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView

class AppListAdapter(
    private val context: Context,
    private val apps: List<ApplicationInfo>,
    private val allowedApps: MutableSet<String>
) : BaseAdapter() {

    private val pm: PackageManager = context.packageManager

    override fun getCount(): Int = apps.size
    override fun getItem(position: Int): Any = apps[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_app_checkbox, parent, false)

        val icon = view.findViewById<ImageView>(R.id.appIcon)
        val name = view.findViewById<TextView>(R.id.appName)
        val checkbox = view.findViewById<CheckBox>(R.id.appCheckbox)

        val app = apps[position]
        val pkgName = app.packageName

        icon.setImageDrawable(pm.getApplicationIcon(app))
        name.text = pm.getApplicationLabel(app)
        checkbox.isChecked = allowedApps.contains(pkgName)

        // реагируем на клики по всей строке
        view.setOnClickListener {
            checkbox.isChecked = !checkbox.isChecked
            toggleApp(pkgName, checkbox.isChecked)
        }

        // или по самому чекбоксу
        checkbox.setOnCheckedChangeListener { _, isChecked ->
            toggleApp(pkgName, isChecked)
        }

        return view
    }

    private fun toggleApp(pkgName: String, enabled: Boolean) {
        if (enabled) allowedApps.add(pkgName) else allowedApps.remove(pkgName)
    }

    fun getSelectedPackages(): Set<String> = allowedApps
}
