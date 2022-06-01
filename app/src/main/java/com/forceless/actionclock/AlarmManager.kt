package com.forceless.actionclock

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.room.Room
import java.util.*


class AlarmManager : Application() {
    companion object{
        lateinit var context:Context
        fun updateAlarm(){
            val current_time = Calendar.getInstance()
            val hour = current_time.get(Calendar.HOUR_OF_DAY)
            val minute = current_time.get(Calendar.MINUTE)
            val dao = context.let {
                Room.databaseBuilder(it, ClockDB::class.java, "Clock")
                    .build()
                    .clockDao()
            }
            val clocks = dao.getAllBackend()
                if (clocks.isNotEmpty()) {
                    var clock:Clock? = null
                    for(temp in clocks){
                        if (temp.enabled&&clock==null){
                            clock=temp
                        }
                        if(temp.enabled&&((temp.hour.toInt()==hour && temp.minute.toInt()> minute)||temp.hour.toInt()>hour))
                        {
                            clock=temp
                            break
                        }
                    }
                    val ID = "com.forceless.alarm"
                    val intentfilter = IntentFilter(ID)
                    intentfilter.addAction(Intent.ACTION_REBOOT)
                    intentfilter.addAction(Intent.ACTION_BOOT_COMPLETED)
                    intentfilter.addAction(Intent.ACTION_SCREEN_OFF)
                    intentfilter.addAction(Intent.ACTION_SCREEN_ON)
                    intentfilter.addAction(Intent.ACTION_SHUTDOWN)
                    context.registerReceiver(BootReceiver(),intentfilter)
                    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
                    val intent = Intent(ID)
                    val REQUEST_CODE = 111131
                    var pendingIntent = PendingIntent.getBroadcast(this.context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                    if (clock != null) {
                        intent.putExtra("class",clock.action_list[0])
                        val exist = (PendingIntent.getBroadcast(context,REQUEST_CODE,intent,PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE) != null )
                        if(exist) {
                            pendingIntent = PendingIntent.getBroadcast(
                                this.context,
                                REQUEST_CODE,
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                            )
                            val calendar = Calendar.getInstance()
                            calendar.set(
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get
                                    (Calendar.DAY_OF_MONTH),
                                clock.hour.toInt(),
                                clock.minute.toInt(),
                                0
                            )
                            alarmManager!!.set(
                                AlarmManager.RTC_WAKEUP,
                                calendar.timeInMillis + 1000,
                                pendingIntent
                            )
                            Log.d("Current Time", System.currentTimeMillis().toString())
                            Log.d("Set Time", calendar.timeInMillis.toString())
                            Log.d(
                                "Clock Reset",
                                calendar.get(Calendar.HOUR_OF_DAY).toString() + calendar.get(
                                    Calendar.MINUTE
                                )
                            )
                        }
                    }
                    else{
                        alarmManager!!.cancel(pendingIntent)
                    }
                }
            }
        }
}