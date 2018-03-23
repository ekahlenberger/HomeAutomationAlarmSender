package net.kahlenberger.eberhard.haas.background

import android.app.job.JobParameters
import android.content.Context

class OpenHabRequestData(url:String, itemName:String, nextAlarm:String, context: Context,requestCount: Int, jParams: JobParameters? = null, service: AlarmUpdateService? = null)
{
    val OpenHabUrl = url
    val ItemName = itemName
    val NextAlarm = nextAlarm
    val Context = context
    val JParams = jParams
    val AlService = service
    val RequestCount = requestCount
}