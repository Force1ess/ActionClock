package com.forceless.actionclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.forceless.com.forceless.actionclock.LanuchAppService


class ClockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("CLock","Received")
        val serviceIntent = Intent(context,LanuchAppService::class.java)
        serviceIntent.putExtra("class",intent.getStringExtra("class"))
        context.startForegroundService(serviceIntent)
        AlarmManager.updateAlarm()
    }
}