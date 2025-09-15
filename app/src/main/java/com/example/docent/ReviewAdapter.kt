package com.example.docent

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// ISO8601 → "yyyy-MM-dd HH:mm"
fun String.toDisplayDate(): String {
    val out = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
    val candidates = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX"
    ).map { java.text.SimpleDateFormat(it, java.util.Locale.US) }
    val trimmed = replace(Regex("\\.(\\d{3})\\d+(?=[Z+\\-])"), ".$1")
    for (p in candidates) {
        try { p.parse(trimmed)?.let { return out.format(it) } } catch (_: Exception) {}
    }
    return this
}

class ReviewAdapter(
    private var items: MutableList<Review>,
    private val authHeader: String?,
    private val currentUserId: Int? = null
) : RecyclerView.Adapter<ReviewAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val content: TextView = v.findViewById(R.id.reviewContent)
        val ratingBar: RatingBar = v.findViewById(R.id.reviewRating)
        val date: TextView = v.findViewById(R.id.reviewDate)
        val authorName: TextView = v.findViewById(R.id.reviewAuthorName)
        val btnDelete: ImageView? = v.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(h: VH, position: Int) {
        val review = items[position]
        h.content.text = review.content
        h.ratingBar.rating = review.rating
        h.date.text = review.created_at.toDisplayDate()
        h.authorName.text = review.author_name

        val canDelete = (currentUserId != null && review.author == currentUserId)
        Log.d("ReviewAdapter",
            "reviewId=${review.id}, author=${review.author}, currentUserId=$currentUserId, canDelete=$canDelete")

        h.btnDelete?.visibility = if (canDelete) View.VISIBLE else View.GONE

        h.btnDelete?.setOnClickListener { view ->
            if (!canDelete) {
                Toast.makeText(view.context, "내 리뷰만 삭제할 수 있어요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (authHeader.isNullOrBlank()) {
                Toast.makeText(view.context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // dp → px 헬퍼
            fun dp(v: Int) = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), view.resources.displayMetrics
            ).toInt()

            // 작은 제목 커스텀
            val titleView = TextView(view.context).apply {
                text = "리뷰 삭제"
                setTextColor(Color.BLACK)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f) // 제목 크기 조절
                setTypeface(typeface, Typeface.BOLD)
                setPadding(dp(24), dp(20), dp(24), 0)
            }

            val dialog = MaterialAlertDialogBuilder(view.context)
                .setCustomTitle(titleView)
                .setMessage("삭제할까요?")
                .setNegativeButton("아니오", null)
                .setPositiveButton("예") { _, _ ->
                    RetrofitClient.instance.deleteReview(authHeader, review.id)
                        .enqueue(object : Callback<ResponseBody> {
                            override fun onResponse(call: Call<ResponseBody>, resp: Response<ResponseBody>) {
                                if (resp.isSuccessful) {
                                    val idx = h.bindingAdapterPosition
                                    if (idx != RecyclerView.NO_POSITION) {
                                        items.removeAt(idx)
                                        notifyItemRemoved(idx)
                                    }
                                    Toast.makeText(view.context, "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                    val msg = if (resp.code() == 403)
                                        "내 리뷰만 삭제할 수 있어요."
                                    else
                                        "삭제 실패: ${resp.code()}"
                                    Toast.makeText(view.context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                Toast.makeText(view.context, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                }
                .create()

            dialog.show()

            // 흰 배경 + 검은 글씨 보정 및 본문/버튼 글씨 크기
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            dialog.findViewById<TextView>(android.R.id.message)?.apply {
                setTextColor(Color.BLACK)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
                setTextColor(Color.BLACK)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            }
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
                setTextColor(Color.BLACK)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    // 리스트 갱신 헬퍼
    fun updateData(newList: List<Review>) {
        items = newList.toMutableList()
        notifyDataSetChanged()
    }
}
