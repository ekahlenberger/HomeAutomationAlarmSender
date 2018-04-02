package net.kahlenberger.eberhard.haas

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.androidapp_list_item.view.*
import net.kahlenberger.eberhard.haas.helpers.IHandleSeenPackages

class AppDataAdapter(var appDataset: Array<AndroidAppData>, val packageHandler: IHandleSeenPackages) : RecyclerView.Adapter<AppDataAdapter.AppDataViewHolder>(){

    class AppDataViewHolder(itemView: View, val packageHandler: IHandleSeenPackages) : RecyclerView.ViewHolder(itemView){
        fun bind(part: AndroidAppData){
            itemView.appNameTextView.text = part.name
            itemView.appIconImageView.contentDescription =  itemView.context.getString(R.string.specific_app_icon,part.name)
            itemView.appPackageTextView.text = part.packageName
            itemView.appIconImageView.setImageDrawable(part.icon)
            itemView.usePackageSwitch.isChecked = part.active
            itemView.usePackageSwitch.setOnClickListener {v ->
                if (part.active)
                    packageHandler.ignorePackage(v.context,part.packageName)
                else
                    packageHandler.allowPackage(v.context, part.packageName)
                part.active = !part.active
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): AppDataViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.androidapp_list_item,parent, false)
        return AppDataViewHolder(view,packageHandler)
    }

    override fun onBindViewHolder(holder: AppDataViewHolder?, position: Int) {
        holder!!.bind(appDataset[position])
    }

    override fun getItemCount() = appDataset.size
}