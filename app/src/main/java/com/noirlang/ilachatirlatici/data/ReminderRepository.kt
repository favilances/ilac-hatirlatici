package com.noirlang.ilachatirlatici.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import java.time.LocalDate
import java.time.DayOfWeek
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderRepository(private val dao: MedicationDao) {
    
    fun remindersForDate(date: LocalDate): LiveData<List<Medication>> {
        return dao.getAllMedications().switchMap { allMedications ->
            val resultLiveData = MutableLiveData<List<Medication>>()
            
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    val filteredMedications = allMedications.filter { medication ->
                        try {
                            when (medication.recurringType) {
                                RecurringType.ONCE -> {
                                    medication.date == date
                                }
                                RecurringType.DAILY -> {
                                    val startDate = medication.startDate ?: medication.date
                                    !date.isBefore(startDate)
                                }
                                RecurringType.WEEKLY -> {
                                    val startDate = medication.startDate ?: medication.date
                                    !date.isBefore(startDate) && date.dayOfWeek == startDate.dayOfWeek
                                }
                                RecurringType.MONTHLY -> {
                                    val startDate = medication.startDate ?: medication.date
                                    !date.isBefore(startDate) && date.dayOfMonth == startDate.dayOfMonth
                                }
                            }
                        } catch (e: Exception) {
                            // Hata durumunda sadece exact date match kontrol et
                            medication.date == date
                        }
                    }
                    
                    resultLiveData.postValue(filteredMedications.sortedBy { it.time })
                }
            } catch (e: Exception) {
                // Hata durumunda boş liste döndür
                resultLiveData.postValue(emptyList())
            }
            
            resultLiveData
        }
    }

    suspend fun addReminder(medication: Medication) = dao.insert(medication)

    suspend fun markCompleted(medication: Medication) = dao.update(medication.copy(completed = true))
    
    suspend fun deleteMedication(medication: Medication) = dao.delete(medication)
} 