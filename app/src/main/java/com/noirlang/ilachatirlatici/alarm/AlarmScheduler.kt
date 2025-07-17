package com.noirlang.ilachatirlatici.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

object AlarmScheduler {

    private const val TAG = "AlarmScheduler"

    fun scheduleExactReminder(context: Context, id: Int, date: LocalDate, time: LocalTime, medicationName: String, dose: String = "1 Tablet", mealTiming: String = "Yemekten Önce") {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            // Create target datetime
            val targetDateTime = LocalDateTime.of(date, time)
            val now = LocalDateTime.now()
            
            // Check if the alarm time is in the future
            if (targetDateTime.isBefore(now)) {
                Log.w(TAG, "Alarm time is in the past: $targetDateTime, scheduling for next day")
                // If time is past, schedule for next day
                val nextDay = targetDateTime.plusDays(1)
                val triggerMillis = nextDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                val uniqueId = generateUniqueAlarmId(id, nextDay.toLocalDate())
                scheduleAlarmInternal(context, alarmManager, uniqueId, triggerMillis, medicationName, dose, mealTiming, id)
                return
            }
            
            val triggerMillis = targetDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val uniqueId = generateUniqueAlarmId(id, date)
            scheduleAlarmInternal(context, alarmManager, uniqueId, triggerMillis, medicationName, dose, mealTiming, id)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm", e)
        }
    }
    
    /**
     * Generate unique alarm ID for each day
     * Formula: originalId * 10000 + dayOfYear
     */
    private fun generateUniqueAlarmId(originalId: Int, date: LocalDate): Int {
        val dayOfYear = date.dayOfYear
        return originalId * 10000 + dayOfYear
    }
    
    private fun scheduleAlarmInternal(context: Context, alarmManager: AlarmManager, uniqueId: Int, triggerMillis: Long, medicationName: String, dose: String, mealTiming: String, originalId: Int) {
        Log.d(TAG, "Scheduling alarm for $medicationName at ${java.util.Date(triggerMillis)} (millis: $triggerMillis, uniqueId: $uniqueId, originalId: $originalId)")

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("med_id", originalId)  // Use original ID for database operations
            putExtra("alarm_id", uniqueId)  // Use unique ID for alarm operations
            putExtra("med_name", medicationName)
            putExtra("med_dose", dose)
            putExtra("meal_timing", mealTiming)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            uniqueId,  // Use unique ID for alarm identification
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Check exact alarm permission for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "Cannot schedule exact alarms - using setAndAllowWhileIdle instead")
                // Use setAndAllowWhileIdle as fallback
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
                )
                Log.d(TAG, "Alarm scheduled with setAndAllowWhileIdle for $medicationName")
                return
            }
        }

        // Schedule exact alarm
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerMillis,
            pendingIntent
        )
        
        Log.d(TAG, "Exact alarm scheduled successfully for $medicationName")
    }

    fun cancelReminder(context: Context, id: Int) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Alarm cancelled for ID: $id")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling alarm", e)
        }
    }
    
    fun cancelTodaysReminder(context: Context, originalId: Int, date: LocalDate) {
        try {
            val uniqueId = generateUniqueAlarmId(originalId, date)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                uniqueId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Today's alarm cancelled for original ID: $originalId, unique ID: $uniqueId")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling today's alarm", e)
        }
    }
} 