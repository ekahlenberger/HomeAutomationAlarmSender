package net.kahlenberger.eberhard.haas

import android.opengl.Visibility
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.activity_manage_alarm_apps.*
import net.kahlenberger.eberhard.haas.helpers.IHandleSeenPackages
import net.kahlenberger.eberhard.haas.helpers.PackageNameSplitterResolver

class ManageAlarmApps : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: AppDataAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val packageHandler: IHandleSeenPackages = PackageNameSplitterResolver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_alarm_apps)

        viewManager = LinearLayoutManager(this)
        viewAdapter = AppDataAdapter(arrayOf(),packageHandler)

        recyclerView = findViewById<RecyclerView>(R.id.appsListView).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        viewAdapter.appDataset = packageHandler.getAlarmAppData(this)
        emptyAppsTextView.visibility = if (viewAdapter.appDataset.size == 0) View.VISIBLE else View.GONE
        recyclerView.visibility = if (viewAdapter.appDataset.size == 0) View.GONE else View.VISIBLE
        viewAdapter.notifyDataSetChanged()
    }
}
