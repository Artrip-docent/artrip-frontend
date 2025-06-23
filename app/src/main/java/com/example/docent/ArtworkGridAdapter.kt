package com.example.docent

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ArtworkGridAdapter(
    private val artworkList: List<Artwork>,
    private val exhibitionId: Int
) : RecyclerView.Adapter<ArtworkGridAdapter.ArtworkViewHolder>() {

    inner class ArtworkViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val artworkImage: ImageView = view.findViewById(R.id.artworkImage)
        val artworkTitle: TextView = view.findViewById(R.id.artworkTitle)
        val artworkArtist: TextView = view.findViewById(R.id.artworkArtist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtworkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_viewedartwork, parent, false)
        return ArtworkViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtworkViewHolder, position: Int) {
        val artwork = artworkList[position]

        holder.artworkTitle.text = artwork.title
        holder.artworkArtist.text = artwork.artist

        Glide.with(holder.itemView.context)
            .load(artwork.image_url)
            .placeholder(R.drawable.art)  // 기본 이미지 리소스
            .error(R.drawable.art)        // 에러 시 이미지
            .into(holder.artworkImage)

        holder.artworkImage.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ChatHistoryActivity::class.java).apply {
                putExtra("artwork_id", artwork.id)
                putExtra("exhibition_id", exhibitionId)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = artworkList.size
}