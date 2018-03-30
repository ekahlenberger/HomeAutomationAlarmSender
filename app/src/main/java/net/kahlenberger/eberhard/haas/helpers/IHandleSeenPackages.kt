package net.kahlenberger.eberhard.haas.helpers

import android.content.Context
import net.kahlenberger.eberhard.haas.AndroidAppData

interface IHandleSeenPackages {
    fun getAlarmAppData(context:Context) : Array<AndroidAppData>
    fun addPackageAndCheckIfAllowed(context: Context, packageName: String): Boolean
    fun ignorePackage(context: Context, packageName: String)
    fun allowPackage(context: Context, packageName: String)
}

