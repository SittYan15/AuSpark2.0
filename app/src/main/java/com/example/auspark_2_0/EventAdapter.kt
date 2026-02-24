package com.example.auspark_2_0

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class EventAdapter(private var items: List<ScheduleUIItem>) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.eventTitle)
        val time: TextView = view.findViewById(R.id.eventTime)
        val type: TextView = view.findViewById(R.id.eventType)
        val location: TextView = view.findViewById(R.id.eventLocation)
        val card: com.google.android.material.card.MaterialCardView = view as com.google.android.material.card.MaterialCardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_item, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.time.text = item.time
        holder.type.text = item.type
        holder.location.text = item.location

        // Visual distinction for Exams using your existing red color
        if (item.type == "Exam") {
            holder.card.strokeColor = ContextCompat.getColor(holder.itemView.context, R.color.au_red)
            holder.card.strokeWidth = 4
        } else {
            holder.card.strokeColor = ContextCompat.getColor(holder.itemView.context, R.color.au_event_border)
            holder.card.strokeWidth = 2
        }
    }

    override fun getItemCount() = items.size

    fun update(newItems: MutableList<ScheduleUIItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
