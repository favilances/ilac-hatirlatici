package com.noirlang.ilachatirlatici.data

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class Converters {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME

    @TypeConverter
    fun fromDate(value: LocalDate?): String? = value?.format(dateFormatter)

    @TypeConverter
    fun toDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it, dateFormatter) }

    @TypeConverter
    fun fromTime(value: LocalTime?): String? = value?.format(timeFormatter)

    @TypeConverter
    fun toTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it, timeFormatter) }
    
    @TypeConverter
    fun fromRecurringType(value: RecurringType?): String? = value?.name
    
    @TypeConverter
    fun toRecurringType(value: String?): RecurringType? = value?.let { RecurringType.valueOf(it) }
    
    @TypeConverter
    fun fromMealTiming(value: MealTiming?): String? = value?.name
    
    @TypeConverter
    fun toMealTiming(value: String?): MealTiming? = value?.let { MealTiming.valueOf(it) }
} 