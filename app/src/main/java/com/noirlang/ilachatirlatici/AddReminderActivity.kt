package com.noirlang.ilachatirlatici

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.noirlang.ilachatirlatici.data.AppDatabase
import com.noirlang.ilachatirlatici.data.Medication
import com.noirlang.ilachatirlatici.data.RecurringType
import com.noirlang.ilachatirlatici.data.MealTiming
import com.noirlang.ilachatirlatici.data.ReminderRepository
import com.noirlang.ilachatirlatici.databinding.ActivityAddReminderBinding
import com.noirlang.ilachatirlatici.alarm.AlarmScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class AddReminderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddReminderBinding
    private lateinit var repository: ReminderRepository

    private var selectedDate: LocalDate? = null
    private var selectedTime: LocalTime? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = ReminderRepository(AppDatabase.getDatabase(this).medicationDao())

        setupClickListeners()
        setupRecurringOptions()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSelectDate.setOnClickListener { showDatePicker() }
        binding.btnSelectTime.setOnClickListener { showTimePicker() }
        binding.btnSave.setOnClickListener { saveReminder() }
    }
    
    private fun setupRecurringOptions() {
        binding.rgRecurring.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                binding.rbOnce.id -> {
                    // Tek seferlik - tarih seçimi gerekli
                    binding.btnSelectDate.isEnabled = true
                    binding.btnSelectDate.alpha = 1.0f
                }
                binding.rbDaily.id -> {
                    // Günlük - tarih seçimi opsiyonel (başlangıç tarihi)
                    binding.btnSelectDate.isEnabled = true
                    binding.btnSelectDate.alpha = 1.0f
                }
                binding.rbWeekly.id -> {
                    // Haftalık - tarih seçimi gerekli (hangi gün)
                    binding.btnSelectDate.isEnabled = true
                    binding.btnSelectDate.alpha = 1.0f
                }
                binding.rbMonthly.id -> {
                    // Aylık - tarih seçimi gerekli (ayın hangi günü)
                    binding.btnSelectDate.isEnabled = true
                    binding.btnSelectDate.alpha = 1.0f
                }
            }
        }
    }

    private fun showDatePicker() {
        val now = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            selectedDate = LocalDate.of(year, month + 1, day)
            updateDateDisplay()
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun showTimePicker() {
        val now = Calendar.getInstance()
        TimePickerDialog(this, { _, hour, minute ->
            selectedTime = LocalTime.of(hour, minute)
            updateTimeDisplay()
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
    }

    private fun updateDateDisplay() {
        selectedDate?.let {
            val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("tr"))
            binding.tvSelectedDate.text = it.format(formatter)
        }
    }

    private fun updateTimeDisplay() {
        selectedTime?.let {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            binding.tvSelectedTime.text = it.format(formatter)
        }
    }

    private fun saveReminder() {
        val name = binding.edtMedName.text.toString().trim()
        val dose = binding.edtDose.text.toString().trim()
        val date = selectedDate
        val time = selectedTime
        
        if (name.isBlank()) {
            binding.edtMedName.error = "İlaç adı gerekli"
            return
        }
        
        if (dose.isBlank()) {
            binding.edtDose.error = "Doz bilgisi gerekli"
            return
        }
        
        if (date == null) {
            // TODO: Show error for date
            return
        }
        
        if (time == null) {
            // TODO: Show error for time
            return
        }

        // Get selected recurring type
        val recurringType = when (binding.rgRecurring.checkedRadioButtonId) {
            binding.rbOnce.id -> RecurringType.ONCE
            binding.rbDaily.id -> RecurringType.DAILY
            binding.rbWeekly.id -> RecurringType.WEEKLY
            binding.rbMonthly.id -> RecurringType.MONTHLY
            else -> RecurringType.ONCE
        }
        
        // Get selected meal timing
        val mealTiming = when (binding.rgMealTiming.checkedRadioButtonId) {
            binding.rbBeforeMeal.id -> MealTiming.BEFORE_MEAL
            binding.rbAfterMeal.id -> MealTiming.AFTER_MEAL
            binding.rbWithMeal.id -> MealTiming.WITH_MEAL
            else -> MealTiming.BEFORE_MEAL
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val med = Medication(
                name = name, 
                dose = dose, 
                date = date, 
                time = time,
                recurringType = recurringType,
                startDate = if (recurringType != RecurringType.ONCE) date else null,
                mealTiming = mealTiming
            )
            
            val id = repository.addReminder(med).toInt()
            
            // Get meal timing text for alarm
            val mealTimingText = when (mealTiming) {
                MealTiming.BEFORE_MEAL -> "Yemekten Önce"
                MealTiming.AFTER_MEAL -> "Yemekten Sonra"
                MealTiming.WITH_MEAL -> "Yemek ile Birlikte"
            }
            
            // Test alarm - 1 dakika sonra test etmek için
            val testTime = LocalTime.now().plusMinutes(1)
            val testDate = LocalDate.now()
            AlarmScheduler.scheduleExactReminder(this@AddReminderActivity, id + 9999, testDate, testTime, "TEST: $name", dose, mealTimingText)
            
            // Schedule the actual alarm
            AlarmScheduler.scheduleExactReminder(this@AddReminderActivity, id, date, time, name, dose, mealTimingText)
            
            runOnUiThread {
                finish()
            }
        }
    }
} 