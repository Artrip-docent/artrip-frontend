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

class ArtRecommendationActivity : AppCompatActivity() {

    private lateinit var exhibitionAdapter: ExhibitionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_art_recommendation)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.mypage).setOnClickListener {
            startActivity(Intent(this, MypageActivity::class.java))
            finish()
        }

        findViewById<ImageView>(R.id.Camera_Button).setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        findViewById<ImageView>(R.id.Commu_Button).setOnClickListener {
            startActivity(Intent(this, communityActivity::class.java))
        }

        // 서버에서 전시회 데이터 가져오기
        fetchExhibitionsFromServer()
    }

    private fun fetchExhibitionsFromServer() {
        val userId = getSharedPreferences("auth", MODE_PRIVATE).getInt("user_id", -1)

        if (userId == -1) {
            Log.e("User", "유저 ID가 유효하지 않음")
            return
        }

        RetrofitClient.instance.getSortedExhibitions(userId).enqueue(object : Callback<List<Exhibition>> {
            override fun onResponse(call: Call<List<Exhibition>>, response: Response<List<Exhibition>>) {
                if (response.isSuccessful) {
                    val exhibitions = response.body() ?: emptyList()

                    exhibitionAdapter = ExhibitionAdapter(
                        exhibitions,
                        { exhibition ->
                            val intent = Intent(this@ArtRecommendationActivity, ReviewActivity::class.java)
                            intent.putExtra("EXHIBITION_ID", exhibition.id)
                            startActivity(intent)
                        },
                        userId
                    )


                    val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
                    recyclerView.adapter = exhibitionAdapter

                } else {
                    Log.e("API", "응답 실패: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Exhibition>>, t: Throwable) {
                Log.e("API", "연결 실패: ${t.message}")
            }
        })
    }


}
