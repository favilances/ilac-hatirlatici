package com.noirlang.ilachatirlatici.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.noirlang.ilachatirlatici.R
import com.noirlang.ilachatirlatici.data.Medication
import com.noirlang.ilachatirlatici.databinding.ItemReminderBinding
import java.time.format.DateTimeFormatter

class ReminderAdapter(
    private val onTakenClick: (Medication) -> Unit,
    private val onDeleteClick: (Medication) -> Unit
) : ListAdapter<Medication, ReminderAdapter.ReminderViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val binding = ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReminderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReminderViewHolder(private val binding: ItemReminderBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(medication: Medication) {
            binding.tvMedicationName.text = medication.name
            binding.tvTime.text = medication.time.format(DateTimeFormatter.ofPattern("HH:mm"))
            binding.tvDosage.text = medication.dose
            
            // Handle completed state
            val context = binding.root.context
            if (medication.completed) {
                binding.btnTaken.setImageResource(R.drawable.ic_check)
                binding.btnTaken.backgroundTintList = context.getColorStateList(R.color.success_green)
                binding.btnTaken.imageTintList = context.getColorStateList(R.color.white)
                binding.btnSkip.alpha = 0.5f
                binding.btnSkip.isEnabled = false
            } else {
                binding.btnTaken.setImageResource(R.drawable.ic_check)
                binding.btnTaken.backgroundTintList = context.getColorStateList(R.color.success_green)
                binding.btnTaken.imageTintList = context.getColorStateList(R.color.white)
                binding.btnSkip.alpha = 1.0f
                binding.btnSkip.isEnabled = true
            }
            
            // Click listeners
            binding.btnTaken.setOnClickListener {
                if (!medication.completed) {
                    onTakenClick(medication)
                }
            }
            
            binding.btnSkip.setOnClickListener {
                // Delete medication completely
                onDeleteClick(medication)
            }
            
            // Card click listener for editing (future feature)
            binding.root.setOnClickListener {
                // TODO: Navigate to edit medication
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Medication>() {
        override fun areItemsTheSame(oldItem: Medication, newItem: Medication): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Medication, newItem: Medication): Boolean {
            return oldItem == newItem
        }
    }
} 