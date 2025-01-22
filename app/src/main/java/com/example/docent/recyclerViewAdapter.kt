package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView



class ArtworkAdapter(
    private val artworks: List<listviewModel>,
    private val itemClickListener: OnItemClickListener  // 클릭 리스너 인터페이스 추가
) : RecyclerView.Adapter<ArtworkAdapter.ArtworkViewHolder>() {

    // 클릭 이벤트 처리를 위한 인터페이스 정의
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    inner class ArtworkViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textView)

        init {
            // 아이템 뷰에 클릭 리스너 설정
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(position)  // 클릭 이벤트 전달
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtworkViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.artviewholder, parent, false)
        return ArtworkViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArtworkViewHolder, position: Int) {
        val artwork = artworks[position] // 리스트에서 해당 아이템 가져오기
        holder.textView.text = artwork.title // textView에 제목 설정
    }

    override fun getItemCount() = artworks.size
}