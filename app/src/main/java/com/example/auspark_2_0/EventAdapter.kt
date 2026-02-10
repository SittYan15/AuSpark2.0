package com.example.auspark_2_0

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventAdapter(private var items: List<EventEntity>) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.eventTitle)
        val time: TextView = view.findViewById(R.id.eventTime)
        val type: TextView = view.findViewById(R.id.eventType)
        val location: TextView = view.findViewById(R.id.eventLocation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.event_item, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.time.text = "${item.startTime} - ${item.endTime}"
        holder.type.text = item.type
        holder.location.text = item.location
    }

    override fun getItemCount() = items.size

    fun update(newItems: List<EventEntity>) {
        items = newItems
        notifyDataSetChanged()
    }
}
