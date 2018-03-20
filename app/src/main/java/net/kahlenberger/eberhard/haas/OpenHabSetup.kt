package net.kahlenberger.eberhard.haas

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class OpenHabSetup : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_hab_setup)

        val pref = getSharedPreferences(getString(R.string.pref_key), Context.MODE_PRIVATE)
        val restUrl = pref.getString(getString(R.string.resturl_key), "")
        val itemName = pref.getString(getString(R.string.item_key), "")
        val error = pref.getString(getString(R.string.error_key), "")

        (findViewById<EditText>(R.id.editURL)).setText(restUrl)
        (findViewById<EditText>(R.id.editItemName)).setText(itemName)
        if (error != "")
            (findViewById<TextView>(R.id.textViewError)).setText("Last request error: " + error)
        else
            (findViewById<TextView>(R.id.textViewError)).setText("")
    }

    override fun onResume() {
        super.onResume()
        showError(getSharedPreferences(getString(R.string.pref_key), Context.MODE_PRIVATE))
    }

    private fun showError(pref:SharedPreferences)
    {
        val error = pref.getString(getString(R.string.error_key),"")
        if (error != "")
            (findViewById<TextView>(R.id.textViewError)).setText("Last request error: " + error)
        else
            (findViewById<TextView>(R.id.textViewError)).setText("")
    }


    fun saveSettings(view: View) {
        val restUrl = (findViewById<EditText>(R.id.editURL)).text.toString()
        val itemName = (findViewById<EditText>(R.id.editItemName)).text.toString()
        val pref: SharedPreferences = getSharedPreferences(getString(R.string.pref_key), Context.MODE_PRIVATE)
        val edit = pref.edit()
        if (edit != null)
        {
            edit.putString(getString(R.string.resturl_key),restUrl)
            edit.putString(getString(R.string.item_key),itemName)
            edit.apply()
            if (restUrl == "" || itemName == "")
                Toast.makeText(this,"alarm sender deactivated",Toast.LENGTH_LONG).show()
            else
                Toast.makeText(this,"alarm sender activated and configured", Toast.LENGTH_LONG).show()
        }
    }
}
