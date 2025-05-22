    package com.example.docent

    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.ImageView
    import android.widget.TextView
    import androidx.recyclerview.widget.RecyclerView
    import com.bumptech.glide.Glide

    data class Exhibition(
        val id: Int,              // 전시회 ID 추가
        val title: String,
        val period: String,
        val location: String,
        val imageUrl: String // 이미지 URL 추가
    )

    class ExhibitionAdapter(
        private var exhibitionList: List<Exhibition>,
        private val onItemClick: (Exhibition) -> Unit  // 클릭 시 실행할 콜백 추가
    ) : RecyclerView.Adapter<ExhibitionAdapter.ViewHolder>() {

        fun updateData(newList: List<Exhibition>) {
            exhibitionList = newList
            notifyDataSetChanged()
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.exhibitionTitle)
            val period: TextView = view.findViewById(R.id.exhibitionPeriod)
            val location: TextView = view.findViewById(R.id.exhibitionLocation)
            val image: ImageView = view.findViewById(R.id.exhibitionImage)

            fun bind(exhibition: Exhibition) {
                title.text = exhibition.title
                period.text = exhibition.period
                location.text = exhibition.location

                Glide.with(itemView.context)
                    .load(exhibition.imageUrl)
                    .placeholder(R.drawable.art)
                    .into(image)

                itemView.setOnClickListener {
                    onItemClick(exhibition)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_exhibition, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(exhibitionList[position])
        }

        override fun getItemCount() = exhibitionList.size
    }

