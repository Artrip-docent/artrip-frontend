package com.example.docent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class Exhibition(
    val title: String,
    val period: String,
    val location: String
)

class ExhibitionAdapter(private var exhibitionList: List<Exhibition>) :
    RecyclerView.Adapter<ExhibitionAdapter.ViewHolder>() {

    fun updateData(newList: List<Exhibition>) {
        exhibitionList = newList
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.exhibitionTitle)
        val period: TextView = view.findViewById(R.id.exhibitionPeriod)
        val location: TextView = view.findViewById(R.id.exhibitionLocation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exhibition, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exhibition = exhibitionList[position]
        holder.title.text = exhibition.title
        holder.period.text = exhibition.period
        holder.location.text = exhibition.location
    }

    override fun getItemCount() = exhibitionList.size
}
