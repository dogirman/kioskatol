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
    private val pm: PackageManager,
    private val appList: MutableList<ApplicationInfo>,
    private val selectedPackages: MutableSet<String>
) : BaseAdapter() {

    private val inflater = LayoutInflater.from(context)

    override fun getCount(): Int = appList.size
    override fun getItem(position: Int): Any = appList[position]
    override fun getItemId(position: Int): Long = position.toLong()

    private class ViewHolder(
        val icon: ImageView,
        val name: TextView,
        val check: CheckBox
    )

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = inflater.inflate(R.layout.app_list_item, parent, false)
            holder = ViewHolder(
                icon = view.findViewById(R.id.appIcon),
                name = view.findViewById(R.id.appName),
                check = view.findViewById(R.id.appCheckBox)
            )
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val appInfo = appList[position]
        val pkg = appInfo.packageName

        holder.name.text = pm.getApplicationLabel(appInfo)
        holder.icon.setImageDrawable(pm.getApplicationIcon(appInfo))

        // предотвращаем срабатывание старого слушателя при переиспользовании view
        holder.check.setOnCheckedChangeListener(null)
        holder.check.isChecked = selectedPackages.contains(pkg)

        // меняем модель при клике
        holder.check.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedPackages.add(pkg) else selectedPackages.remove(pkg)
        }

        // удобство: клик по строке тоже переключает чек
        view.setOnClickListener {
            val newState = !holder.check.isChecked
            holder.check.isChecked = newState // вызовет слушатель и обновит selectedPackages
        }

        return view
    }

    // Возвращаем текущий набор выбранных пакетов (мутируемый — можно сразу сохранить)
    fun getSelectedPackages(): MutableSet<String> = selectedPackages
}
