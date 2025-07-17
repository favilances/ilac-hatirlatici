package com.noirlang.ilachatirlatici

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.noirlang.ilachatirlatici.databinding.ActivityPermissionsBinding

class PermissionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPermissionsBinding
    private companion object {
        const val TAG = "PermissionsActivity"
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { _ ->
            checkCompletion()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (getSharedPreferences("prefs", MODE_PRIVATE).getBoolean("permissions_granted", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding = ActivityPermissionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGrant.setOnClickListener {
            requestNeededPermissions()
        }
    }

    private fun requestNeededPermissions() {
        Log.d(TAG, "Requesting permissions...")
        
        // Notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Overlay permission
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
        
        // Exact alarm permission for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.d(TAG, "Requesting exact alarm permission...")
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
        
        // Check completion after a short delay
        binding.root.postDelayed({ checkCompletion() }, 1000)
    }

    private fun checkCompletion() {
        val notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else true

        val overlayGranted = Settings.canDrawOverlays(this)
        
        val exactAlarmGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else true

        Log.d(TAG, "Permission status - Notification: $notificationGranted, Overlay: $overlayGranted, ExactAlarm: $exactAlarmGranted")

        if (notificationGranted && overlayGranted && exactAlarmGranted) {
            getSharedPreferences("prefs", MODE_PRIVATE).edit().putBoolean("permissions_granted", true).apply()
            Log.d(TAG, "All permissions granted, starting MainActivity")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Check permissions when user returns from settings
        checkCompletion()
    }
} 