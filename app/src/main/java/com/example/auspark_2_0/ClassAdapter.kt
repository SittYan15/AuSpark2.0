package com.example.auspark_2_0

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ClassAdapter(private var classList: List<ClassEntity>) : RecyclerView.Adapter<ClassAdapter.ClassViewHolder>() {

    class ClassViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvCourseCode: TextView = view.findViewById(R.id.tvCourseCode)
        val tvCourseName: TextView = view.findViewById(R.id.tvCourseName)
        val tvInstructor: TextView = view.findViewById(R.id.tvInstructor)
        val tvRoom: TextView = view.findViewById(R.id.tvRoom)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_class, parent, false)
        return ClassViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        val item = classList[position]

        holder.tvTime.text = "${item.startTime} - ${item.endTime}"
        holder.tvCourseCode.text = "${item.courseCode} (${item.section})"
        holder.tvCourseName.text = item.courseName
        holder.tvInstructor.text = item.instructor
        holder.tvRoom.text = item.room
    }

    override fun getItemCount() = classList.size

    fun updateDate(newClasses: List<ClassEntity>) {
        classList = newClasses
        notifyDataSetChanged()
    }
}
