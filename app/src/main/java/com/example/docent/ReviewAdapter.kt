package com.example.docent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*



fun String.toDisplayDate(): String {
    val out = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
    val candidates = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX"
    ).map { java.text.SimpleDateFormat(it, java.util.Locale.US) }

    // 마이크로초(6자리 이상) 들어오면 3자리(ms)까지만 남기기
    val trimmed = replace(Regex("\\.(\\d{3})\\d+(?=[Z+\\-])"), ".$1")
    for (p in candidates) {
        try {
            val d = p.parse(trimmed)
            if (d != null) return out.format(d)
        } catch (_: Exception) {}
    }
    return this // 실패하면 원문 그대로
}

// RecyclerView Adapter 클래스
class ReviewAdapter(private var reviewList: List<Review>) :
    RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    fun updateData(newList: List<Review>) {
        reviewList = newList
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val content: TextView = view.findViewById(R.id.reviewContent)
        val ratingBar: RatingBar = view.findViewById(R.id.reviewRating)
        val date: TextView = view.findViewById(R.id.reviewDate)
        val authorName: TextView = view.findViewById(R.id.reviewAuthorName)  // 추가
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = reviewList[position]
        holder.content.text = review.content
        holder.ratingBar.rating = review.rating
        holder.date.text = review.created_at.toDisplayDate()
        holder.authorName.text = review.author_name  // 여기서 바인딩
    }

    override fun getItemCount() = reviewList.size
}

