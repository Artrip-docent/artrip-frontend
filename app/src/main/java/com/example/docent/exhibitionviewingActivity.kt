package com.example.docent

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.docent.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class exhibitionviewingActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exhibitionviewing)

        recyclerView = findViewById(R.id.exhibitionRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // SharedPreferences에서 user_id 가져오기
        prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)

        // userId 유효성 검사 후 API 호출
        if (userId != -1) {
            fetchViewedExhibitions(userId)
        } else {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
        }

        val arrowImage = findViewById<ImageView>(R.id.arrow4)
        arrowImage.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchViewedExhibitions(userId: Int) {
        val token = prefs.getString("accessToken", null)

        if (token.isNullOrBlank()) {
            Toast.makeText(this, "인증 토큰이 없습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val bearerToken = "Bearer $token"
        val call = RetrofitClient.instance.getViewedExhibitions(bearerToken, userId)

        call.enqueue(object : Callback<List<ViewedExhibition>> {
            override fun onResponse(
                call: Call<List<ViewedExhibition>>,
                response: Response<List<ViewedExhibition>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val exhibitions = response.body()!!
                    recyclerView.adapter = ViewedExhibitionAdapter(exhibitions)
                } else {
                    Toast.makeText(this@exhibitionviewingActivity, "데이터를 불러오지 못했습니다", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<ViewedExhibition>>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(this@exhibitionviewingActivity, "네트워크 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
            }
        })
    }
}