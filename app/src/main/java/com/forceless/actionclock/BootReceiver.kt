package com.forceless.actionclock

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        AlarmManager.context = p0!!
        Log.d("ActionClock"+"Boot Receiver","received")
        MainScope().launch(Dispatchers.IO) {
            val ID = "com.forceless.alarm"
            val intent = Intent(ID)
            val REQUEST_CODE = 111131
            val exist = (PendingIntent.getBroadcast(
                AlarmManager.context,REQUEST_CODE,intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE) != null )
            if (!exist) AlarmManager.updateAlarm()
        }
    }
}