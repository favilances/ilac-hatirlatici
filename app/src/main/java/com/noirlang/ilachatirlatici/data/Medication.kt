package com.noirlang.ilachatirlatici.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

enum class RecurringType {
    ONCE,    // Sadece bu tarihte
    DAILY,   // Her gün bu saatte
    WEEKLY,  // Her hafta bu günde
    MONTHLY  // Her ay bu tarihte
}

enum class MealTiming {
    BEFORE_MEAL,  // Yemekten Önce
    AFTER_MEAL,   // Yemekten Sonra
    WITH_MEAL     // Yemek ile Birlikte
}

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dose: String,
    val date: LocalDate, // scheduled date
    val time: LocalTime, // scheduled time
    val completed: Boolean = false,
    val recurringType: RecurringType = RecurringType.ONCE,
    val startDate: LocalDate? = null, // başlangıç tarihi recurring için
    val mealTiming: MealTiming = MealTiming.BEFORE_MEAL
) 