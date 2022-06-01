package com.forceless.actionclock

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.WindowManager
import androidx.core.content.contentValuesOf
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        AlarmManager.context = p0!!
        MainScope().launch(Dispatchers.IO) {
            AlarmManager.updateAlarm()
        }
    }
}