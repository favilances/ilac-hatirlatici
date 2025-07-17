package com.noirlang.ilachatirlatici.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.noirlang.ilachatirlatici.R

class ReminderForegroundService : Service() {

    private val channelId = "med_alarm_channel"
    private companion object {
        const val TAG = "ReminderForegroundService"
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            val medId = intent?.getIntExtra("med_id", -1) ?: -1
            val medName = intent?.getStringExtra("med_name") ?: "İlaç"
            val medDose = intent?.getStringExtra("med_dose") ?: "1 Tablet"

            Log.d(TAG, "Starting foreground service for medication: $medName")

            val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
                putExtra("med_id", medId)
                putExtra("med_name", medName)
                putExtra("med_dose", medDose)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            
            val fullScreenPendingIntent = PendingIntent.getActivity(
                this,
                0,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification: Notification = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_medication)
                .setContentTitle("İlaç Hatırlatıcısı")
                .setContentText("$medName zamanı!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setAutoCancel(true)
                .build()

            startForeground(1, notification)
            Log.d(TAG, "Notification shown for medication: $medName")

            // Launch activity immediately
            try {
                startActivity(fullScreenIntent)
                Log.d(TAG, "AlarmActivity launched for medication: $medName")
            } catch (e: Exception) {
                Log.e(TAG, "Error launching AlarmActivity", e)
            }

            // Stop service after launching activity
            stopSelf()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in onStartCommand", e)
            stopSelf()
        }
        
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    channelId,
                    "İlaç Alarmları",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    setShowBadge(true)
                    setSound(null, null)
                    enableVibration(true)
                }
                
                val nm = getSystemService(NotificationManager::class.java)
                nm?.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating notification channel", e)
            }
        }
    }
} 