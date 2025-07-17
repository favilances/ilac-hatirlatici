package com.noirlang.ilachatirlatici.data

import androidx.lifecycle.LiveData
import androidx.room.*
import java.time.LocalDate

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications WHERE date = :date ORDER BY time ASC")
    fun getMedicationsForDate(date: LocalDate): LiveData<List<Medication>>
    
    @Query("SELECT * FROM medications ORDER BY date ASC, time ASC")
    fun getAllMedications(): LiveData<List<Medication>>

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationById(id: Int): Medication?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: Medication): Long

    @Update
    suspend fun update(medication: Medication)

    @Delete
    suspend fun delete(medication: Medication)
} 