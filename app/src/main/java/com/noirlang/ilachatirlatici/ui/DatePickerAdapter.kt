package com.noirlang.ilachatirlatici.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.noirlang.ilachatirlatici.R
import com.noirlang.ilachatirlatici.databinding.ItemDateBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

data class DateItem(
    val date: LocalDate,
    val dayName: String,
    val dayNumber: String,
    val isSelected: Boolean = false
)

class DatePickerAdapter(
    private val onDateSelected: (LocalDate) -> Unit
) : RecyclerView.Adapter<DatePickerAdapter.DateViewHolder>() {

    private var dates = mutableListOf<DateItem>()
    private var selectedPosition = -1

    init {
        generateDates()
    }

    private fun generateDates() {
        dates.clear()
        val today = LocalDate.now()
        
        // Generate 30 days starting from today
        for (i in 0..29) {
            val date = today.plusDays(i.toLong())
            val dayName = date.dayOfWeek.name.substring(0, 3).uppercase()
            val dayNumber = date.dayOfMonth.toString()
            
            dates.add(DateItem(
                date = date,
                dayName = dayName,
                dayNumber = dayNumber,
                isSelected = i == 0 // Today is selected by default
            ))
        }
        
        selectedPosition = 0
        notifyDataSetChanged()
    }

    fun selectDate(position: Int) {
        if (selectedPosition != position) {
            val oldPosition = selectedPosition
            selectedPosition = position
            
            notifyItemChanged(oldPosition)
            notifyItemChanged(selectedPosition)
            
            onDateSelected(dates[position].date)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val binding = ItemDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        holder.bind(dates[position], position == selectedPosition)
    }

    override fun getItemCount() = dates.size

    inner class DateViewHolder(private val binding: ItemDateBinding) : RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                selectDate(bindingAdapterPosition)
            }
        }

        fun bind(dateItem: DateItem, isSelected: Boolean) {
            binding.tvDayName.text = dateItem.dayName
            binding.tvDayNumber.text = dateItem.dayNumber
            
            binding.root.isSelected = isSelected
            
            // Update text colors based on selection
            val context = binding.root.context
            if (isSelected) {
                binding.tvDayName.setTextColor(context.getColor(R.color.white))
                binding.tvDayNumber.setTextColor(context.getColor(R.color.white))
            } else {
                binding.tvDayName.setTextColor(context.getColor(R.color.text_secondary))
                binding.tvDayNumber.setTextColor(context.getColor(R.color.text_primary))
            }
        }
    }
} 