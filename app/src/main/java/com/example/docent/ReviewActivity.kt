package com.example.docent

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

class ReviewActivity : AppCompatActivity() {


    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        recyclerView = findViewById(R.id.reviewRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ReviewAdapter(emptyList())
        recyclerView.adapter = adapter

        val exhibitionId = intent.getIntExtra("EXHIBITION_ID", -1)
        if (exhibitionId == -1) {
            Toast.makeText(this, "전시회 ID가 유효하지 않습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val preview = findViewById<ImageView>(R.id.iv_back)
        preview.setOnClickListener{
            val intent = Intent(this, ArtRecommendationActivity::class.java)
            startActivity(intent)
        }
        fetchReviews(exhibitionId)

        var next_review = findViewById<Button>(R.id.btn_review_next)
        next_review.setOnClickListener {
            val intent = Intent(this, ReviewWriteActivity::class.java)
            intent.putExtra("EXHIBITION_ID", exhibitionId)
            startActivity(intent)
        }
    }

    private fun fetchReviews(exhibitionId: Int) {
        val reviewApi = RetrofitClient.instance

        reviewApi.getReviewsByExhibition(exhibitionId).enqueue(object : Callback<List<Review>> {
            override fun onResponse(call: Call<List<Review>>, response: Response<List<Review>>) {
                if (response.isSuccessful) {
                    val reviews = response.body() ?: emptyList()
                    adapter.updateData(reviews)
                } else {
                    Toast.makeText(this@ReviewActivity, "서버 오류: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Review>>, t: Throwable) {
                Toast.makeText(this@ReviewActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("ReviewActivity", "onFailure: ${t.message}")
            }
        })



    }

}
