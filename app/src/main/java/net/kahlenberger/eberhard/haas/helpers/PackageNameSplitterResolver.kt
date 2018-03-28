package net.kahlenberger.eberhard.haas.helpers

import android.content.Context
import android.content.pm.PackageManager
import net.kahlenberger.eberhard.haas.R

class PackageNameSplitterResolver : IHandleSeenPackages {
    override fun getAppNames(context: Context, packageList: String): Array<String> {
        val packages = packageList.split(";")
        val pm = context.packageManager
        val apps:MutableList<String> = mutableListOf()
        for (packageName in packages){
            try {
                val info = pm.getApplicationInfo(packageName,0)
                val appName = pm.getApplicationLabel(info).toString()
                apps.add(appName)
            }
            catch (ex: PackageManager.NameNotFoundException){

            }
        }
        return apps.toTypedArray()
    }
    override fun addPackageIfNew(packageList: String, newPackage: String) : String{
        if (packageList.split(";").any({p -> p == newPackage })) return packageList
        if (packageList.isNullOrBlank()) return newPackage
        return packageList + ";" + newPackage
    }
    override fun addPackageAndCheckIfAllowed(context:Context, packageName: String) : Boolean{
        val pref = context.getSharedPreferences(context.getString(R.string.pref_key),Context.MODE_PRIVATE)
        val packages = addPackageIfNew(pref.getString("knownPackages",""),packageName)
        val packageIsIgnored = pref.getString("ignoredPackages","").split(";").any({p -> p == packageName})
        if  (packageIsIgnored) return false

        with(pref.edit())
        {
            putString("knownPackages",packages)
            apply()
        }
        return true
    }
}