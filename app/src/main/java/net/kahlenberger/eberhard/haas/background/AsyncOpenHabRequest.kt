package net.kahlenberger.eberhard.haas.background

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.job.JobInfo
import android.app.job.JobInfo.Builder
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.AsyncTask
import android.os.PersistableBundle
import android.widget.Toast
import net.kahlenberger.eberhard.haas.helpers.IProvideFreeJobId
import net.kahlenberger.eberhard.haas.R
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class AsyncOpenHabRequest(val jobIdProvider: IProvideFreeJobId) : AsyncTask<OpenHabRequestData, String, OpenHABResponse>()
{
    @SuppressLint("StaticFieldLeak")

    override fun onPostExecute(result: OpenHABResponse) {
        if (result.context != null) {
            when (result.type) {
                OpenHABResponseType.Success -> Toast.makeText(result.context, result.context.getString(R.string.toast_sendSuccess), Toast.LENGTH_SHORT).show()
                OpenHABResponseType.Denied -> Toast.makeText(result.context, result.context.getString(R.string.toast_sendDenied), Toast.LENGTH_LONG).show()
                    //OpenHABResponseType.FailureRetry -> Toast.makeText(context, context.getString(R.string.toast_sendFailureRetry), Toast.LENGTH_LONG).show()
                else -> { }
            }
        }
    }

    override fun doInBackground(vararg params: OpenHabRequestData?): OpenHABResponse {

        val request = params.first()

        if (request != null)
        {
            if (isCancelled) return OpenHABResponse(OpenHABResponseType.NotDone,request.context)

            val am = request.context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val nextAlarm = am.nextAlarmClock
            var sendAlarm = "0"
            if (nextAlarm != null) sendAlarm = (nextAlarm.triggerTime / 1000).toString()
            val response = sendOpenhabReq(request.openHabUrl,request.itemName, sendAlarm ,request.context)
            if (request.jParams != null && request.alService != null)
            {
                request.alService.jobFinished(request.jParams,false)
            }
            return OpenHABResponse(response,request.context)
        }
        return OpenHABResponse(OpenHABResponseType.NotDone,null)
    }

    private fun sendOpenhabReq(restUrl: String, itemName: String, payload: String, context: Context) : OpenHABResponseType {
        var connection: HttpURLConnection? =  null
        try
        {
            val url = URL("$restUrl/items/$itemName/state")

            connection = url.openConnection() as HttpURLConnection

            connection.setRequestProperty("Content-Type", "text/plain; charset=UTF-8")
            connection.setRequestProperty("Content-Length", payload.length.toString())
            connection.requestMethod = "PUT"
            connection.doOutput = true
            connection.instanceFollowRedirects = false
            connection.connect()
            val writer = OutputStreamWriter(connection.outputStream, "UTF-8")
            writer.write(payload)
            writer.close()
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_ACCEPTED)
            {
                setPreference(context, R.string.pref_key, R.string.error_key,"")
                return OpenHABResponseType.Success
            }
            setPreference(context, R.string.pref_key, R.string.error_key,"Target responded with " + responseCode.toString())
            return OpenHABResponseType.Denied
        }
        catch (ex:Exception)
        {
            setPreference(context, R.string.pref_key, R.string.error_key,ex.message + "trying again")
            createJob(restUrl, itemName, context)
            return OpenHABResponseType.FailureRetry
        }
        finally {
            if (connection != null)
                connection.disconnect()
        }
    }
    @SuppressLint("ApplySharedPref")
    private fun setPreference(context: Context, pref_key: Int, key: Int, value: String?)
    {
        val pref = context.getSharedPreferences(context.getString(pref_key), Context.MODE_PRIVATE)
        val edit = pref.edit()
        edit.putString(context.getString(key),value)
        edit.commit()
    }

    private fun createJob(restUrl: String, itemName: String, context: Context){
        val newJobId = jobIdProvider.getFreeJobId(context)
        val job = Builder(newJobId, ComponentName(context,AlarmUpdateService::class.java))
        job.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
        //job.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        val extras = PersistableBundle()
        extras.putString("url",restUrl)
        extras.putString("item", itemName)
        job.setRequiresCharging(false).setRequiresDeviceIdle(false).setExtras(extras).setMinimumLatency(30000)

        (context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as  JobScheduler).schedule(job.build())
    }



}