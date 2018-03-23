package net.kahlenberger.eberhard.haas.background

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import net.kahlenberger.eberhard.haas.R

class AlarmChangedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "android.app.action.NEXT_ALARM_CLOCK_CHANGED") return

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextAlarm = am.nextAlarmClock
        val pref = context.getSharedPreferences(context.getString(R.string.pref_key),Context.MODE_PRIVATE)
        val restUrl:String  = pref.getString(context.getString(R.string.resturl_key),"")
        val itemName:String = pref.getString(context.getString(R.string.item_key),"")

        if (restUrl != "" && itemName != "")
            if (nextAlarm == null)
                AsyncOpenHabRequest().execute(OpenHabRequestData(restUrl, itemName, "0", context,0))
            else
                AsyncOpenHabRequest().execute(OpenHabRequestData(restUrl, itemName, (nextAlarm.triggerTime / 1000).toString(), context,0))

    }
}
