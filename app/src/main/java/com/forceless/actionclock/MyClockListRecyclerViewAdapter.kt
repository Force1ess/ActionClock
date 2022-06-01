package com.forceless.actionclock

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.forceless.actionclock.databinding.ClockItemBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MyClockListRecyclerViewAdapter(
    private val dao: ClockDao,
    private var values: List<Clock>,
    private val parentFragmentManager: FragmentManager
) : RecyclerView.Adapter<MyClockListRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            ClockItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var hour = values[position].hour
        var minute = values[position].minute
        if (hour.length == 1){
            hour="0"+hour
        }
        if (minute.length == 1){
            minute="0"+minute
        }
        holder.time_text.text = holder.itemView.context.getString(R.string.time_format, hour, minute)
        holder.switch.isChecked = values[position].enabled
        holder.clock.hours = hour.toInt()
        holder.clock.minutes = minute.toInt()
        holder.switch.setOnClickListener {
            values[position].enabled = !values[position].enabled
            MainScope().launch(Dispatchers.IO) {
                dao.update(values[position])
                AlarmManager.updateAlarm()
            }
        }
        holder.clock.setOnClickListener {
            val picker = MaterialTimePicker.Builder()
                .setHour(hour.toInt())
                .setMinute(minute.toInt())
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText("change clock time")
                .build()
            picker.show(this.parentFragmentManager, "tag")
            picker.addOnPositiveButtonClickListener {
                var reshour=  picker.hour.toString()
                if(reshour.length<2){
                    reshour="0"+reshour
                }
                var resminute =  picker.minute.toString()
                if(resminute.length<2){
                    resminute="0"+resminute
                }
                values[position].hour= reshour
                values[position].minute = resminute
                MainScope().launch(Dispatchers.IO) {
                    dao.update(values[position])
                    AlarmManager.updateAlarm()
                }
            }
        }
        holder.clock.setOnLongClickListener {
            MaterialAlertDialogBuilder(holder.clock.context)
                .setNegativeButton("No") { _, _ ->
                }
                .setPositiveButton("Yes") { _, _ ->
                    MainScope().launch(Dispatchers.IO) {
                        dao.delete(values[position])
                        AlarmManager.updateAlarm()
                    }
                }
                .setTitle("是否删除？")
                .show()
            true
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: ClockItemBinding) : RecyclerView.ViewHolder(binding.root) {
        var time_text = binding.time
        var switch = binding.switcher
        var clock = binding.clocks
    }
}