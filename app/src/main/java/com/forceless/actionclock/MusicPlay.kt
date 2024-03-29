package com.forceless.actionclock
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.IBinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MusicPlay : Service() {
    var player = MediaPlayer()
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationChannel = NotificationChannel("123456","Alarm", NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.importance = NotificationManager.IMPORTANCE_HIGH
        notificationChannel.enableVibration(true)
        notificationChannel.enableLights(true)
        val notification: Notification = Notification.Builder(this,"123456")
            .setContentTitle("Alarm Launched")
            .setSmallIcon(R.drawable.ic_timer_notify)
            .build()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)
        startForeground(114514, notification)
        player.reset()
        player.setDataSource(intent!!.getStringExtra("path"))
        player.isLooping = true
        player.prepare()
        player.start()
        MainScope().launch(Dispatchers.IO) {
            delay(500)
            val am = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            while(am!!.getStreamVolume(AudioManager.STREAM_MUSIC)!=am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)){
            am.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0)
            }
        }
        return super.onStartCommand(intent, flags, startId)

    }
}