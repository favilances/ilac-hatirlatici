package com.noirlang.ilachatirlatici.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.noirlang.ilachatirlatici.R
import com.noirlang.ilachatirlatici.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    
    private companion object {
        const val TAG = "AlarmReceiver"
        const val CHANNEL_ID = "med_alarm_channel"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        try {
            val medId = intent.getIntExtra("med_id", -1)
            val alarmId = intent.getIntExtra("alarm_id", -1)
            val medName = intent.getStringExtra("med_name") ?: "İlaç"
            val medDose = intent.getStringExtra("med_dose") ?: "1 Tablet"
            val mealTiming = intent.getStringExtra("meal_timing") ?: "Yemekten Önce"
            
            Log.d(TAG, "Alarm received for medication: $medName (MedID: $medId, AlarmID: $alarmId)")
            
            if (medId == -1) {
                Log.e(TAG, "Invalid medication ID")
                return
            }
            
            // Check if medication is already completed
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val dao = AppDatabase.getDatabase(context).medicationDao()
                    val medication = dao.getMedicationById(medId)
                    
                    if (medication == null) {
                        Log.e(TAG, "Medication not found in database: $medId")
                        return@launch
                    }
                    
                    if (medication.completed) {
                        Log.d(TAG, "Medication already completed, skipping alarm: $medName")
                        return@launch
                    }
                    
                    // Medication is not completed, proceed with alarm
                    CoroutineScope(Dispatchers.Main).launch {
                        showAlarmNotification(context, medId, medName, medDose, mealTiming)
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error checking medication status", e)
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing alarm", e)
        }
    }
    
    private fun showAlarmNotification(context: Context, medId: Int, medName: String, medDose: String, mealTiming: String) {
        try {
            // Create notification channel
            createNotificationChannel(context)
            
            // Create intent for AlarmActivity
            val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
                putExtra("med_id", medId)
                putExtra("med_name", medName)
                putExtra("med_dose", medDose)
                putExtra("meal_timing", mealTiming)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            
            // Create pending intent
            val pendingIntent = PendingIntent.getActivity(
                context,
                medId,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Create notification
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_medication)
                .setContentTitle("İlaç Hatırlatıcısı")
                .setContentText("$medName zamanı! $medDose alın.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, true)
                .setAutoCancel(true)
                .build()
            
            // Show notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(medId, notification)
            
            // Launch AlarmActivity directly
            context.startActivity(alarmIntent)
            
            Log.d(TAG, "Alarm notification and activity launched for: $medName")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing alarm notification", e)
        }
    }
    
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "İlaç Alarmları",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    setShowBadge(true)
                    enableVibration(true)
                    description = "İlaç hatırlatıcı alarmları"
                }
                
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating notification channel", e)
            }
        }
    }
} 