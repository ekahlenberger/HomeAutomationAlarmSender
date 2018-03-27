package net.kahlenberger.eberhard.haas.background

import android.app.job.JobParameters
import android.content.Context

class OpenHabRequestData(val openHabUrl:String, val itemName: String, val context: Context, val jParams: JobParameters? = null, val alService: AlarmUpdateService? = null)
