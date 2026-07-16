package com.example.interfaces

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.interfaces.ui.booking.BookingUtils

class SlotAdapter(
    private val onSlotSelected: (SlotSelectionViewModel.Slot) -> Unit
) : RecyclerView.Adapter<SlotAdapter.SlotViewHolder>() {

    private var slots: List<SlotSelectionViewModel.Slot> = emptyList()
    private var selectedPosition: Int = 0

    fun submitList(newSlots: List<SlotSelectionViewModel.Slot>) {
        slots = newSlots
        selectedPosition = 0
        if (slots.isNotEmpty()) {
            onSlotSelected(slots[0])
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_slot, parent, false)
        return SlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        val slot = slots[position]
        val isSelected = position == selectedPosition
        holder.bind(slot, isSelected)
        
        holder.itemView.setOnClickListener {
            val previousSelected = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previousSelected)
            notifyItemChanged(selectedPosition)
            onSlotSelected(slot)
        }
    }

    override fun getItemCount(): Int = slots.size

    class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card = itemView.findViewById<CardView>(R.id.card_slot)
        private val txtDate = itemView.findViewById<TextView>(R.id.txt_date)
        private val txtTime = itemView.findViewById<TextView>(R.id.txt_time)
        private val txtStatus = itemView.findViewById<TextView>(R.id.txt_status)

        fun bind(slot: SlotSelectionViewModel.Slot, isSelected: Boolean) {
            txtDate.text = BookingUtils.formatIsoDateForDisplay(slot.date)
            txtTime.text = slot.time
            
            if (isSelected) {
                card.setCardBackgroundColor(Color.parseColor("#173B63"))
                txtDate.setTextColor(Color.WHITE)
                txtTime.setTextColor(Color.WHITE)
                txtStatus.visibility = View.VISIBLE
                txtStatus.text = "✓"
                txtStatus.setTextColor(Color.WHITE)
                txtStatus.setBackgroundColor(Color.parseColor("#5E8F73"))
            } else {
                card.setCardBackgroundColor(Color.WHITE)
                txtDate.setTextColor(Color.parseColor("#17324F"))
                txtTime.setTextColor(Color.parseColor("#6F7782"))
                txtStatus.visibility = View.GONE
            }
        }
    }
}
