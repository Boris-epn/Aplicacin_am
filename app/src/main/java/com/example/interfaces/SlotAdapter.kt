package com.example.interfaces

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.interfaces.ui.booking.BookingUtils
import java.time.LocalTime

class SlotAdapter(
    private val onTimeSelected: (LocalTime) -> Unit
) : RecyclerView.Adapter<SlotAdapter.SlotViewHolder>() {

    private var times: List<LocalTime> = emptyList()
    private var selectedPosition: Int = 0

    fun submitList(newTimes: List<LocalTime>) {
        times = newTimes
        selectedPosition = 0
        if (times.isNotEmpty()) {
            onTimeSelected(times[0])
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_slot, parent, false)
        return SlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        val time = times[position]
        val isSelected = position == selectedPosition
        holder.bind(time, isSelected)

        holder.itemView.setOnClickListener {
            val previousSelected = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previousSelected)
            notifyItemChanged(selectedPosition)
            onTimeSelected(time)
        }
    }

    override fun getItemCount(): Int = times.size

    class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card = itemView.findViewById<CardView>(R.id.card_slot)
        private val txtTime = itemView.findViewById<TextView>(R.id.txt_time)
        private val txtStatus = itemView.findViewById<TextView>(R.id.txt_status)

        fun bind(time: LocalTime, isSelected: Boolean) {
            txtTime.text = BookingUtils.formatTimeForDisplay(time)

            if (isSelected) {
                card.setCardBackgroundColor(Color.parseColor("#173B63"))
                txtTime.setTextColor(Color.WHITE)
                txtStatus.visibility = View.VISIBLE
                txtStatus.text = "✓"
                txtStatus.setTextColor(Color.WHITE)
                txtStatus.setBackgroundColor(Color.parseColor("#5E8F73"))
            } else {
                card.setCardBackgroundColor(Color.WHITE)
                txtTime.setTextColor(Color.parseColor("#17324F"))
                txtStatus.visibility = View.GONE
            }
        }
    }
}
