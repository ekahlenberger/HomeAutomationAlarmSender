package net.kahlenberger.eberhard.haas.helpers

import android.content.Context


interface IProvideFreeJobId {
    fun getFreeJobId(context: Context) : Int
}

