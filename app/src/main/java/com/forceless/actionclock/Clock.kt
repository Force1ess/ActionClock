package com.forceless.actionclock

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.util.Log
import androidx.room.*
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import java.util.*

class Converter {
    val gson = Gson()

    @TypeConverter
    fun fromValue(value: List<String>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun fromJson(json: String): List<String> {
        return gson.fromJson(json, Array<String>::class.java).toList()
    }
}

@Entity()
data class Clock(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    var hour: String,
    var minute:String,
    var action_list: List<String>,
    var enabled: Boolean
)

@Dao
interface ClockDao {
    @Query("SELECT * FROM CLOCK ORDER BY hour, minute")
    fun getAll(): Flow<List<Clock>>
    @Insert
    fun insert(clock: Clock)
    @Update
    fun update(clock: Clock)
    @Delete
    fun delete(clock: Clock)

    @Query("SELECT * FROM CLOCK ORDER BY hour, minute")
    fun getAllBackend():List<Clock>
}

@Database(entities = [Clock::class], version = 1, exportSchema = false)
@TypeConverters(Converter::class)
abstract class ClockDB() : RoomDatabase() {
    abstract fun clockDao(): ClockDao
}
