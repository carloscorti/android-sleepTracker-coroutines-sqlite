package com.example.android.trackmysleepquality.sleeptracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.databinding.ListItemSleepNightBinding

class SleepNightAdapter(private val sleepNightListener: SleepNightListener) : ListAdapter<SleepNight, SleepNightAdapter.ViewHolder>(SleepNightDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, sleepNightListener)
    }

    class ViewHolder private constructor(private val binding: ListItemSleepNightBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SleepNight, sleepNightListener: SleepNightListener) {
            binding.sleepNight = item
            binding.clickListener = sleepNightListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
//                val binding = DataBindingUtil.inflate<ListItemSleepNightBinding>(
//                        LayoutInflater.from(parent.context), R.layout.list_item_sleep_night, parent, false)

                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemSleepNightBinding.inflate(
                        layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }
}

class SleepNightDiffCallback : DiffUtil.ItemCallback<SleepNight>() {
    override fun areItemsTheSame(oldItem: SleepNight, newItem: SleepNight) = oldItem.nightId == newItem.nightId

    override fun areContentsTheSame(oldItem: SleepNight, newItem: SleepNight) = oldItem == newItem
}

class SleepNightListener(val clickCallback: (nightId: Long) -> Unit) {
    fun onClick(sleepNight: SleepNight) = clickCallback(sleepNight.nightId)
}