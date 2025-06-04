package com.example.docent

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReviewWriteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_write)

        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val reviewEditText = findViewById<EditText>(R.id.reviewEditText)
        val submitButton = findViewById<Button>(R.id.submitReviewButton)

        val exhibitionId = intent.getIntExtra("EXHIBITION_ID", -1)


        submitButton.setOnClickListener {
            val content = reviewEditText.text.toString().trim()
            val rating = ratingBar.rating

            if (content.isEmpty() || rating == 0f) {
                Toast.makeText(this, "리뷰와 평점을 모두 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (exhibitionId == -1) {
                Toast.makeText(this, "유효하지 않은 전시회입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = ReviewRequest(exhibition = exhibitionId, content = content, rating = rating)

            RetrofitClient.instance.postReview(request).enqueue(object : Callback<Review> {
                override fun onResponse(call: Call<Review>, response: Response<Review>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ReviewWriteActivity, "리뷰가 등록되었습니다.", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                            Toast.makeText(this@ReviewWriteActivity, "등록 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Review>, t: Throwable) {
                    Toast.makeText(this@ReviewWriteActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("ReviewWrite", "onFailure: ${t.message}")
                }
            })
        }

    }
}
