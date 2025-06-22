package com.example.docent

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class Exhibition(
    val id: Int,              // 전시회 ID 추가
    val title: String,
    val period: String,
    val location: String,
    val imageUrl: String, // 이미지 URL 추가
    var liked: Boolean = false // 하트 토글 여부 저장
)

class ExhibitionAdapter(
    private var exhibitionList: List<Exhibition>,
    private val onItemClick: (Exhibition) -> Unit,  // 클릭 시 실행할 콜백 추가
    private val userId: Int, // 사용자 ID 추가
    private val showHeartIcon: Boolean = true
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
        val heartIcon: ImageView = view.findViewById(R.id.heartIcon)
        val reviewButton: View = view.findViewById(R.id.review)



        fun bind(exhibition: Exhibition) {
            title.text = exhibition.title
            period.text = exhibition.period
            location.text = exhibition.location



            Glide.with(itemView.context)
                .load(exhibition.imageUrl)
                .placeholder(R.drawable.art)
                .into(image)

            // 하트 상태 설정
            if (showHeartIcon) {
                heartIcon.visibility = View.VISIBLE
                heartIcon.setImageResource(
                    if (exhibition.liked) R.drawable.heart_filled else R.drawable.heart_empty
                )

                // 하트 클릭 시 서버에 toggle 요청
                heartIcon.setOnClickListener {
                    if (userId == -1) {
                        Log.e("LikeAPI", "Invalid userId: $userId")
                        return@setOnClickListener
                    }

                    val payload = JsonObject().apply {
                        addProperty("user_id", userId)
                        addProperty("exhibition_id", exhibition.id)
                    }

                    RetrofitClient.instance.toggleLike(payload).enqueue(object :
                        Callback<JsonObject> {
                        override fun onResponse(
                            call: Call<JsonObject>,
                            response: Response<JsonObject>
                        ) {
                            if (response.isSuccessful) {
                                val liked = response.body()?.get("liked")?.asBoolean ?: false
                                exhibition.liked = liked

                                val pos = bindingAdapterPosition
                                if (pos != RecyclerView.NO_POSITION) {
                                    notifyItemChanged(pos)
                                }
                            } else {
                                Log.e(
                                    "LikeAPI",
                                    "서버 응답 오류: ${response.code()} ${response.message()}"
                                )
                            }
                        }

                        override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                            Log.e("LikeAPI", "서버 연결 실패: ${t.localizedMessage}")
                        }
                    })
                }
            } else {
                heartIcon.visibility = View.GONE
            }

            reviewButton.setOnClickListener {
                val intent = Intent(itemView.context, ReviewActivity::class.java)
                intent.putExtra("EXHIBITION_ID", exhibition.id)
                itemView.context.startActivity(intent)
            }






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
