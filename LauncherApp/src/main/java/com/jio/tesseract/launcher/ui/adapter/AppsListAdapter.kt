package com.jio.tesseract.launcher.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jio.tesseract.launcher.databinding.ItemAppInfoBinding
import com.jio.tesseract.launcher.ui.adapter.AppsListAdapter.AppInfoHolder
import com.jio.tesseract.launchersdk.AppInfo

/**
 * Launcher Apps Listing adapter
 * @property listener Function1<[@kotlin.ParameterName] AppInfo, Unit>
 * @constructor
 */
class AppsListAdapter(private val listener: (appInfo: AppInfo) -> Unit) : ListAdapter<AppInfo, AppInfoHolder>(
    ITEM_COMPARATOR
) {
    var appInfoOrgList: MutableList<AppInfo>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppInfoHolder {
        val binding = ItemAppInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppInfoHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: AppInfoHolder, position: Int) {
        val repoItem = getItem(position)
        holder.bind(repoItem)
    }

    /**
     *
     * @param list MutableList<AppInfo>?
     */
    fun submitOrgList(list: MutableList<AppInfo>?) {
        appInfoOrgList = list
        submitList(list)
    }

    fun reSubmitOrgList(){
        submitList(appInfoOrgList)
    }

    companion object {
        private val ITEM_COMPARATOR = object : DiffUtil.ItemCallback<AppInfo>() {
            override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean =
                oldItem.packageName == newItem.packageName

            override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean =
                oldItem == newItem
        }
    }

    class AppInfoHolder(private val binding: ItemAppInfoBinding, private val listener: (item: AppInfo) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(appInfo: AppInfo) {
            binding.appInfo = appInfo
            binding.root.setOnClickListener {
                listener.invoke(appInfo)
            }
            binding.executePendingBindings()
        }
    }
}
