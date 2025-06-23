package com.example.docent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
data class ViewedExhibition(
    val title: String,
    val start_date: String,
    val end_date: String,
    val location: String,
    val image_url: String?
)
class ViewedExhibitionAdapter(
    private val exhibitionList: List<ViewedExhibition>
) : RecyclerView.Adapter<ViewedExhibitionAdapter.ExhibitionViewHolder>() {

    class ExhibitionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.exhibitionTitle)
        val period: TextView = view.findViewById(R.id.exhibitionPeriod)
        val location: TextView = view.findViewById(R.id.exhibitionLocation)
        val image: ImageView = view.findViewById(R.id.exhibitionImage)
        val heartIcon: ImageView = view.findViewById(R.id.heartIcon)
        val reviewButton: TextView = view.findViewById(R.id.review)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExhibitionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_viewedexhibition, parent, false)
        return ExhibitionViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExhibitionViewHolder, position: Int) {
        val exhibition = exhibitionList[position]
        holder.title.text = exhibition.title
        holder.period.text = "기간: ${exhibition.start_date} ~ ${exhibition.end_date}"
        holder.location.text = "장소: ${exhibition.location}"

        // Glide로 이미지 로딩
        Glide.with(holder.itemView.context)
            .load(exhibition.image_url)
            .placeholder(R.drawable.art)
            .into(holder.image)
    }

    override fun getItemCount(): Int = exhibitionList.size
}
