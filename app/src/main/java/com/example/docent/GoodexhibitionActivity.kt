package com.example.docent

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GoodexhibitionActivity : AppCompatActivity() {

    private lateinit var exhibitionAdapter: ExhibitionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_goodexhibition)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val arrowImage = findViewById<ImageView>(R.id.arrow6)
        arrowImage.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }

        setupRecyclerView()
        loadLikedExhibitions()
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.goodExhibitionRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
    }

    private fun loadLikedExhibitions() {
        val userId = getSharedPreferences("auth", MODE_PRIVATE).getInt("user_id", -1)

        RetrofitClient.instance.getSortedExhibitions(userId).enqueue(object : Callback<List<Exhibition>> {
            override fun onResponse(call: Call<List<Exhibition>>, response: Response<List<Exhibition>>) {
                if (response.isSuccessful) {
                    val allExhibitions = response.body() ?: emptyList()
                    val likedExhibitions = allExhibitions.filter { it.liked }

                    exhibitionAdapter = ExhibitionAdapter(
                        likedExhibitions,
                        { /* 전시 클릭 시 상세 보기 생략 가능 */ },
                        userId,
                        showHeartIcon = false // 좋아요 화면에선 하트 숨김

                    )

                    findViewById<RecyclerView>(R.id.goodExhibitionRecyclerView).adapter = exhibitionAdapter
                } else {
                    Log.e("GoodExhibition", "응답 실패: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Exhibition>>, t: Throwable) {
                Log.e("GoodExhibition", "네트워크 실패: ${t.message}")
            }
        })
    }

}