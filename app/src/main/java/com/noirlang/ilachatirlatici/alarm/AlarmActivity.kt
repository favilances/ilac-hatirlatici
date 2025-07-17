package com.noirlang.ilachatirlatici.alarm

import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.noirlang.ilachatirlatici.databinding.ActivityAlarmBinding
import com.noirlang.ilachatirlatici.data.AppDatabase
import com.noirlang.ilachatirlatici.data.ReminderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmBinding
    private var ringtone: Ringtone? = null
    private lateinit var repository: ReminderRepository
    private var medicationId: Int = -1
    
    private companion object {
        const val TAG = "AlarmActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            Log.d(TAG, "AlarmActivity started")
            
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_FULLSCREEN
            )

            binding = ActivityAlarmBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Initialize repository
            repository = ReminderRepository(AppDatabase.getDatabase(this).medicationDao())

            // Get medication details from intent
            medicationId = intent.getIntExtra("med_id", -1)
            val medName = intent.getStringExtra("med_name") ?: "İlaç"
            val medDose = intent.getStringExtra("med_dose") ?: "1 Tablet"
            val mealTiming = intent.getStringExtra("meal_timing") ?: "Yemekten Önce"
            
            Log.d(TAG, "Alarm for medication: $medName (ID: $medicationId)")
            
            // Set medication name
            binding.tvMedName.text = medName
            
            // Set current time (24-hour format)
            try {
                val currentTime = LocalTime.now()
                binding.tvTime.text = currentTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            } catch (e: Exception) {
                Log.e(TAG, "Error formatting time", e)
                binding.tvTime.text = "00:00"
            }
            
            // Set instruction text based on medication dose and meal timing
            binding.tvInstruction.text = "$medDose Al\n$mealTiming"
            
            // Set day info
            binding.tvDayInfo.text = "HATIRLATICI"

            // Start ringtone
            startRingtone()

            // Close button functionality
            binding.btnClose.setOnClickListener { 
                Log.d(TAG, "Close button clicked")
                stopRingtone()
                finish() 
            }
            
            // Setup button functionality
            setupButtons()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in AlarmActivity onCreate", e)
            finish()
        }
    }
    
    private fun startRingtone() {
        try {
            val ringtoneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            
            ringtone = RingtoneManager.getRingtone(this, ringtoneUri)
            ringtone?.play()
            Log.d(TAG, "Ringtone started")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting ringtone", e)
        }
    }
    
    private fun stopRingtone() {
        try {
            ringtone?.stop()
            ringtone = null
            Log.d(TAG, "Ringtone stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping ringtone", e)
        }
    }
    
    private fun setupButtons() {
        // Take medication button
        binding.btnTakeMedication.setOnClickListener {
            Log.d(TAG, "Take medication button clicked")
            markMedicationAsTaken()
        }
        
        // Dismiss button
        binding.btnDismiss.setOnClickListener {
            Log.d(TAG, "Dismiss button clicked")
            stopRingtone()
            finish()
        }
    }
    
    private fun markMedicationAsTaken() {
        Log.d(TAG, "Medication marked as taken: $medicationId")
        
        if (medicationId != -1) {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    // Get medication from database
                    val dao = AppDatabase.getDatabase(this@AlarmActivity).medicationDao()
                    val medication = dao.getMedicationById(medicationId)
                    
                    if (medication != null) {
                        // Mark medication as completed in database
                        repository.markCompleted(medication)
                        
                        // Cancel only today's alarm to prevent duplicate alarms
                        // This allows recurring alarms to continue for other days
                        AlarmScheduler.cancelTodaysReminder(this@AlarmActivity, medicationId, LocalDate.now())
                        
                        Log.d(TAG, "Medication successfully marked as taken and today's alarm cancelled")
                    }
                    
                    runOnUiThread {
                        stopRingtone()
                        finish()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error marking medication as taken", e)
                    runOnUiThread {
                        stopRingtone()
                        finish()
                    }
                }
            }
        } else {
            stopRingtone()
            finish()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopRingtone()
        Log.d(TAG, "AlarmActivity destroyed")
    }
    
    override fun onPause() {
        super.onPause()
        stopRingtone()
    }
} 