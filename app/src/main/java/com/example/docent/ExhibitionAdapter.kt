package com.example.docent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

data class Exhibition(
    val title: String,
    val period: String,
    val location: String,
    val imageUrl: String // 이미지 URL 추가
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
        val image: ImageView = view.findViewById(R.id.exhibitionImage) // 이미지 뷰 추가
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

        // Glide를 사용해서 이미지 로드
        Glide.with(holder.itemView.context)
            .load(exhibition.imageUrl)
            .placeholder(R.drawable.art) // 로딩 중 표시할 이미지
            .into(holder.image)
    }

    override fun getItemCount() = exhibitionList.size
}
