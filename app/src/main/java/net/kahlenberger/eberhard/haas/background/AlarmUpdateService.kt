package net.kahlenberger.eberhard.haas.background

import android.app.job.JobParameters
import android.app.job.JobService
import net.kahlenberger.eberhard.haas.helpers.MaxJobIdIncrementProvider

class AlarmUpdateService : JobService() {
    private val jobIdProvider = MaxJobIdIncrementProvider()
    private var runningRequest:AsyncOpenHabRequest?  = null

    override fun onStartJob(param: JobParameters?): Boolean {
        val extras = param!!.extras
        val url = extras.getString("url")
        val item = extras.getString("item")
        runningRequest = AsyncOpenHabRequest(jobIdProvider)
        runningRequest!!.execute(OpenHabRequestData(url, item, applicationContext,param,this))
        return false
    }

    override fun onStopJob(p0: JobParameters?): Boolean {
        if (runningRequest == null) return true
        runningRequest!!.cancel(true)
        return true
    }
}