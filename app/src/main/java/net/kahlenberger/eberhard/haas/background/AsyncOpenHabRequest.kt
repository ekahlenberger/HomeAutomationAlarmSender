package net.kahlenberger.eberhard.haas.background

import android.annotation.SuppressLint
import android.app.job.JobInfo
import android.app.job.JobInfo.Builder
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.AsyncTask
import android.os.PersistableBundle
import android.widget.Toast
import net.kahlenberger.eberhard.haas.R
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class AsyncOpenHabRequest : AsyncTask<OpenHabRequestData, String, OpenHABResponse>()
{
    @SuppressLint("StaticFieldLeak")
    private var callerContext: Context? = null
    override fun onPostExecute(result: OpenHABResponse) {
        val context = callerContext!!
        callerContext = null
        when (result) {
            OpenHABResponse.Success -> Toast.makeText(context, context.getString(R.string.toast_sendSuccess), Toast.LENGTH_SHORT).show()
            OpenHABResponse.Denied -> Toast.makeText(context, context.getString(R.string.toast_sendDenied), Toast.LENGTH_LONG).show()
            //OpenHABResponse.FailureRetry -> Toast.makeText(context, context.getString(R.string.toast_sendFailureRetry), Toast.LENGTH_LONG).show()
            else -> {}
        }

    }

    override fun doInBackground(vararg params: OpenHabRequestData?): OpenHABResponse {
        if (isCancelled) return OpenHABResponse.NotDone

        val request = params.first()

        if (request != null)
        {
            callerContext = request.Context
            val response = sendOpenhabReq(request.OpenHabUrl,request.ItemName,request.NextAlarm,request.Context, request.JParams == null || request.AlService == null)
            if (request.JParams != null && request.AlService != null)
            {
                request.JParams.extras.putInt("requestCount",request.JParams.extras.getInt("requestCount",1) + 1)
                request.AlService.jobFinished(request.JParams,response == OpenHABResponse.FailureRetry)
            }
            return response
        }
        return OpenHABResponse.NotDone
    }

    private fun sendOpenhabReq(restUrl: String, itemName: String, payload: String, context: Context, jobOnError: Boolean) :OpenHABResponse   {
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
                return OpenHABResponse.Success
            }
            setPreference(context, R.string.pref_key, R.string.error_key,"Target responded with " + responseCode.toString())
            return OpenHABResponse.Denied
        }
        catch (ex:Exception)
        {
            setPreference(context, R.string.pref_key, R.string.error_key,ex.message + "trying again")
            if (jobOnError)
                createJob(restUrl, itemName, payload, context)
            return OpenHABResponse.FailureRetry
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

    private fun createJob(restUrl: String, itemName: String, payload: String, context: Context){
        val job = Builder(1, ComponentName(context,AlarmUpdateService::class.java))
        job.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
        val extras = PersistableBundle()
        extras.putString("url",restUrl)
        extras.putString("item", itemName)
        extras.putString("payload",payload)
        extras.putInt("requestCount",1)
        job.setRequiresCharging(false).setRequiresDeviceIdle(false).setExtras(extras).setMinimumLatency(30000)

        (context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as  JobScheduler).schedule(job.build())
    }



}