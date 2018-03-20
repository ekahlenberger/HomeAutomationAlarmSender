package net.kahlenberger.eberhard.haas

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.widget.Toast
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class AlarmChangedReceiver : BroadcastReceiver() {

    private inner class AsyncOpenHabRequest : AsyncTask<OpenHabrequestData, String, Boolean>()
    {
        var CallerContext:Context? = null
        override fun onPostExecute(result: Boolean?) {
            if (result!!)
                Toast.makeText(CallerContext!!,"new alarm sent to openHab instance", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(CallerContext!!,"alarm not sent to openHab instance due to an error", Toast.LENGTH_LONG).show()
        }

        override fun doInBackground(vararg params: OpenHabrequestData?): Boolean {

            val request = params.first()

            if (request != null)
            {
                CallerContext = request.Context
                return SendOpenhabReq(request.OpenHabUrl,request.ItemName,request.NextAlarm,request.Context)
            }
            return false
        }

        private fun SendOpenhabReq(restUrl: String, itemName: String, payload: String, context: Context) :Boolean   {
            var connection: HttpURLConnection? =  null
            try
            {
                val url = URL(restUrl + "/items/" + itemName + "/state")

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
                    SetPreference(context,R.string.pref_key,R.string.error_key,"")
                    return true;
                }
                SetPreference(context,R.string.pref_key,R.string.error_key,"Target responded with " + responseCode.toString())
                return false;
            }
            catch (ex:Exception)
            {
                SetPreference(context,R.string.pref_key,R.string.error_key,ex.message)
                return false
            }
            finally {
                if (connection != null)
                    connection.disconnect()
            }
        }
        private fun SetPreference(context: Context,pref_key: Int, key: Int,value: String?)
        {
            val pref = context.getSharedPreferences(context.getString(pref_key),Context.MODE_PRIVATE)
            with(pref.edit())
            {
                putString(context.getString(key),value)
                commit()
            }
        }



    }

    private inner  class OpenHabrequestData(url:String, itemName:String, nextAlarm:String, context:Context)
    {
        val OpenHabUrl = url
        val ItemName = itemName
        val NextAlarm = nextAlarm
        val Context = context
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "android.app.action.NEXT_ALARM_CLOCK_CHANGED") return

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextAlarm = am.nextAlarmClock
        val pref = context.getSharedPreferences(context.getString(R.string.pref_key),Context.MODE_PRIVATE)
        val restUrl:String  = pref.getString(context.getString(R.string.resturl_key),"")
        val itemName:String = pref.getString(context.getString(R.string.item_key),"")

        if (restUrl != "" && itemName != "")
            if (nextAlarm == null)
                AsyncOpenHabRequest().execute(OpenHabrequestData(restUrl,itemName,"0",context))
            else
                AsyncOpenHabRequest().execute(OpenHabrequestData(restUrl,itemName,(nextAlarm.triggerTime / 1000).toString(),context))

    }
}
