package com.forceless.actionclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.forceless.com.forceless.actionclock.LanuchAppService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ClockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ActionClock"+"CLock","Received")
        val serviceIntent = Intent(context,LanuchAppService::class.java)
        serviceIntent.putExtra("class",intent.getStringExtra("class"))
        context.startForegroundService(serviceIntent)
        MainScope().launch(Dispatchers.IO) {
            delay(10000)
            AlarmManager.updateAlarm()
        }
    }
}