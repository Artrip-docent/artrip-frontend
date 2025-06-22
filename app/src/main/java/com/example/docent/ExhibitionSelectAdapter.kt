package com.example.docent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExhibitionSelectAdapter(
    private val exhibitionList: List<Exhibition>,
    private val onItemClick: (Int) -> Unit  // 선택한 exhibition_id 전달
) : RecyclerView.Adapter<ExhibitionSelectAdapter.ViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tvTitle)
        private val location: TextView = itemView.findViewById(R.id.tvLocation)

        fun bind(exhibition: Exhibition, position: Int) {
            title.text = exhibition.title
            location.text = exhibition.location

            // 선택 시 배경 변경
            itemView.isSelected = position == selectedPosition
            itemView.setBackgroundColor(
                if (itemView.isSelected) 0xFFE0E0E0.toInt() else 0xFFFFFFFF.toInt()
            )

            itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onItemClick(exhibition.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exhibition_select, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(exhibitionList[position], position)
    }

    override fun getItemCount(): Int = exhibitionList.size
}
