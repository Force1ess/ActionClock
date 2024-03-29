package com.forceless.actionclock

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.forceless.actionclock.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)
        val permissions = arrayOf(Manifest.permission.CAMERA
            ,Manifest.permission.RECORD_AUDIO,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.USE_FULL_SCREEN_INTENT,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.SET_ALARM)
        PermissionRequest.getPermission(this,permissions)
        Log.d("ActionClock"+"Alarm","Started")
        AlarmManager.context=this
        MainScope().launch(Dispatchers.IO) {
            AlarmManager.updateAlarm()
        }
        val intentfilter = IntentFilter()
        intentfilter.addAction(Intent.ACTION_REBOOT)
        intentfilter.addAction(Intent.ACTION_BOOT_COMPLETED)
        intentfilter.addAction(Intent.ACTION_SCREEN_OFF)
        intentfilter.addAction(Intent.ACTION_SCREEN_ON)
        intentfilter.addAction("restartservice")
        intentfilter.addAction(Intent.ACTION_SHUTDOWN)
        AlarmManager.context.registerReceiver(BootReceiver(), intentfilter)


    }



    override fun onDestroy() {
        super.onDestroy()
        val broadcastIntent = Intent()
        broadcastIntent.action = "restartservice"
        broadcastIntent.setClass(this, BootReceiver::class.java)
        this.sendBroadcast(broadcastIntent)
    }
}

