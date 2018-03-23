package net.kahlenberger.eberhard.haas.background

import android.app.job.JobParameters
import android.app.job.JobService

class AlarmUpdateService : JobService() {

    private var runningRequest:AsyncOpenHabRequest?  = null
    override fun onStartJob(param: JobParameters?): Boolean {
        val extras = param!!.extras
        val url = extras.getString("url")
        val item = extras.getString("item")
        val payload = extras.getString("payload")
        val requestCount = extras.getInt("requestCount")
        runningRequest = AsyncOpenHabRequest()
        runningRequest!!.execute(OpenHabRequestData(url, item, payload, applicationContext,requestCount,param,this))
        return false
    }

    override fun onStopJob(p0: JobParameters?): Boolean {
        if (runningRequest == null) return true
        runningRequest!!.cancel(true)
        return true
    }
}