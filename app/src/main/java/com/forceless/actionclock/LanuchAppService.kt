package com.forceless.com.forceless.actionclock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.forceless.actionclock.AlarmManager.Companion.context
import com.forceless.actionclock.MainActivity
import com.forceless.actionclock.R
import com.forceless.actionclock.WakeupActivity


class LanuchAppService: Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationChannel = NotificationChannel("114514","Alarm",NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.importance = NotificationManager.IMPORTANCE_HIGH
        notificationChannel.enableVibration(true)
        notificationChannel.enableLights(true)
        val notification: Notification = Notification.Builder(this,"114514")
            .setContentTitle("Alarm Launched")
            .setSmallIcon(R.drawable.ic_timer_notify)
            .build()
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
        Log.d("Launch APP service ", "received ")
        startForeground(12345, notification)
        val inte = Intent(this, WakeupActivity::class.java)
        inte.flags = FLAG_ACTIVITY_NEW_TASK
        inte.putExtra("class",intent!!.getStringExtra("class"))
        startActivity(inte)
        return super.onStartCommand(intent, flags, startId)
    }
}