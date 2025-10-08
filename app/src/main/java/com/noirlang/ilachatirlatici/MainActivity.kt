package com.noirlang.ilachatirlatici

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
// AdMob integrations removed
import android.util.Log
import com.noirlang.ilachatirlatici.databinding.ActivityMainBinding
import com.noirlang.ilachatirlatici.data.AppDatabase
import com.noirlang.ilachatirlatici.data.ReminderRepository
import com.noirlang.ilachatirlatici.ui.ReminderAdapter
import com.noirlang.ilachatirlatici.ui.DatePickerAdapter
import java.time.LocalDate
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.widget.LinearLayout
import com.noirlang.ilachatirlatici.R
import android.widget.TextView
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: ReminderRepository
    private lateinit var reminderAdapter: ReminderAdapter
    private lateinit var datePickerAdapter: DatePickerAdapter
    private var selectedDate: LocalDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize repository
        repository = ReminderRepository(AppDatabase.getDatabase(this).medicationDao())

        // Google Mobile Ads SDK removed. No ads will be shown.

        // Setup date picker
        setupDatePicker()
        
        // Setup reminders list
        setupRemindersList()
        
        // Load reminders for selected date
        loadRemindersForDate(selectedDate)

        // Bottom navigation "Ekle" button - add click listener to the entire LinearLayout
        binding.root.findViewById<LinearLayout>(R.id.layoutBottomNavAdd)?.setOnClickListener {
            startActivity(Intent(this, AddReminderActivity::class.java))
        }
    }
    
    private fun setupDatePicker() {
        datePickerAdapter = DatePickerAdapter { date ->
            selectedDate = date
            loadRemindersForDate(date)
            updateDateHeader(date)
        }
        
        binding.rvDatePicker.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = datePickerAdapter
        }
        
        // Initialize with today's date
        updateDateHeader(selectedDate)
    }
    
    private fun updateDateHeader(date: LocalDate) {
        val today = LocalDate.now()
        val headerText = if (date == today) {
            "Bugünün Hatırlatıcıları"
        } else {
            val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("tr"))
            date.format(formatter)
        }
        
        // Update the header text in the reminders card
        binding.root.findViewById<TextView>(R.id.tvRemindersHeader)?.text = headerText
    }

    private fun setupRemindersList() {
        reminderAdapter = ReminderAdapter(
            onTakenClick = { medication ->
                lifecycleScope.launch(Dispatchers.IO) {
                    repository.markCompleted(medication)
                    runOnUiThread {
                        loadRemindersForDate(selectedDate)
                    }
                }
            },
            onDeleteClick = { medication ->
                // Delete medication completely
                lifecycleScope.launch(Dispatchers.IO) {
                    repository.deleteMedication(medication)
                    runOnUiThread {
                        loadRemindersForDate(selectedDate)
                    }
                }
            }
        )

        binding.rvReminders.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = reminderAdapter
        }
    }
    
    private fun loadRemindersForDate(date: LocalDate) {
        repository.remindersForDate(date).observe(this, Observer { medications ->
            reminderAdapter.submitList(medications)

            // Show/hide empty state
            if (medications.isEmpty()) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.rvReminders.visibility = View.GONE
            } else {
                binding.layoutEmptyState.visibility = View.GONE
                binding.rvReminders.visibility = View.VISIBLE
            }
        })
    }
    
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || 
               networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this activity
        loadRemindersForDate(selectedDate)
    }
}