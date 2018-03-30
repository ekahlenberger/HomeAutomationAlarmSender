package net.kahlenberger.eberhard.haas.helpers

import android.content.Context
import android.content.pm.PackageManager
import net.kahlenberger.eberhard.haas.AndroidAppData
import net.kahlenberger.eberhard.haas.R

class PackageNameSplitterResolver : IHandleSeenPackages {

    override fun allowPackage(context: Context, packageName: String) {
        val pref = context.getSharedPreferences(context.getString(R.string.pref_key),Context.MODE_PRIVATE)
        val packageList = pref.getString(context.getString(R.string.knownPackages_key), "")
        val ignoredList = pref.getString(context.getString(R.string.ignoredPackages_key), "")
        val ignoredPackages = ignoredList.split(";")
        val newIgnoredPackages =  ignoredPackages.filterNot { i -> i == packageName}.joinToString(";")
        if (newIgnoredPackages != ignoredList)
            with(pref.edit()) {
                putString(context.getString(R.string.ignoredPackages_key),newIgnoredPackages)
                val newKnownPackages = addPackageIfNew(packageList,packageName)
                if (packageList != newKnownPackages)
                    putString(context.getString(R.string.knownPackages_key),newKnownPackages)
                apply()
            }

    }

    override fun ignorePackage(context: Context, packageName: String) {
        val pref = context.getSharedPreferences(context.getString(R.string.pref_key), Context.MODE_PRIVATE)
        val packageList = pref.getString(context.getString(R.string.knownPackages_key), "")
        val ignoredList = pref.getString(context.getString(R.string.ignoredPackages_key), "")
        val ignoredPackages = ignoredList.split(";")
        if (ignoredPackages.any({ p -> p == packageName })) return
        val newIgnoredPackageList = addPackageIfNew(ignoredList, packageName)
        if (newIgnoredPackageList != ignoredList) {
            with(pref.edit()) {
                putString(context.getString(R.string.ignoredPackages_key), newIgnoredPackageList)
                val newPackageList = addPackageIfNew(packageList, packageName)
                if (newPackageList != packageList)
                    putString(context.getString(R.string.knownPackages_key), newPackageList)
                apply()
            }
        }
    }

    override fun getAlarmAppData(context: Context): Array<AndroidAppData> {
        val pref = context.getSharedPreferences(context.getString(R.string.pref_key),Context.MODE_PRIVATE)
        val packages = pref.getString(context.getString(R.string.knownPackages_key),"").split(";")
        val ignoredPackages = pref.getString(context.getString(R.string.ignoredPackages_key), "").split(";")

        val pm = context.packageManager
        val apps:MutableList<AndroidAppData> = mutableListOf()
        for (packageName in packages){
            try {
                val info = pm.getApplicationInfo(packageName,0)
                val appName = pm.getApplicationLabel(info).toString()
                val icon = pm.getApplicationIcon(packageName)
                apps.add(AndroidAppData(appName,packageName,icon,ignoredPackages.none({p -> p == packageName})))
            }
            catch (ex: PackageManager.NameNotFoundException){

            }
        }
        val appsArr = apps.toTypedArray()
        appsArr.sortBy { p -> p.name }
        return appsArr
    }

    override fun addPackageAndCheckIfAllowed(context:Context, packageName: String) : Boolean{
        val pref = context.getSharedPreferences(context.getString(R.string.pref_key),Context.MODE_PRIVATE)
        val packageList = pref.getString(context.getString(R.string.knownPackages_key), "")
        val newPackageList = addPackageIfNew(packageList,packageName)
        val packageIsIgnored = pref.getString(context.getString(R.string.ignoredPackages_key),"").split(";").any({p -> p == packageName})
        if  (packageIsIgnored) return false

        if (packageList != newPackageList) {
            with(pref.edit()) {
                putString(context.getString(R.string.knownPackages_key), newPackageList)
                apply()
            }
        }
        return true
    }

    private fun addPackageIfNew(packageList: String, newPackage: String) : String{
        if (packageList.split(";").any({p -> p == newPackage })) return packageList
        if (packageList.isBlank()) return newPackage
        return packageList + ";" + newPackage
    }
}