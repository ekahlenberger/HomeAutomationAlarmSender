package net.kahlenberger.eberhard.haas.helpers

import android.app.job.JobScheduler
import android.content.Context

class MaxJobIdIncrementProvider : IProvideFreeJobId {
    override fun getFreeJobId(context: Context) : Int {
        val scheduler = (context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler)
        var maxId  = 0
        for (job in scheduler.allPendingJobs)
        {
            if (job.id > maxId) maxId =  job.id
        }
        return ++maxId
    }
}