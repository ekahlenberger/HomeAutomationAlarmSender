package net.kahlenberger.eberhard.haas.helpers

import android.content.Context

interface IHandleSeenPackages {
    fun getAppNames(context:Context, packageList: String) : Array<String>
    fun addPackageIfNew(packageList: String, newPackage: String) : String
    fun addPackageAndCheckIfAllowed(context: Context, packageName: String): Boolean
}

