package com.example.docent

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
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
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchInput: EditText
    private lateinit var searchBtn: TextView
    private lateinit var cancelBtn: TextView

    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_art_recommendation)

        // 뷰 초기화
        recyclerView = findViewById(R.id.recyclerView)
        searchInput = findViewById(R.id.search_input)
        searchBtn = findViewById(R.id.tv_search)
        cancelBtn = findViewById(R.id.tv_cancel)

        recyclerView.layoutManager = GridLayoutManager(this, 2)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.nav_profile).setOnClickListener {
            startActivity(Intent(this, MypageActivity::class.java))
            finish()
        }

        findViewById<ImageView>(R.id.nav_camera).setOnClickListener {
            startActivity(Intent(this, ExhibitionSelectActivity::class.java))
        }

        userId = getSharedPreferences("auth", MODE_PRIVATE).getInt("user_id", -1)

        if (userId == -1) {
            Log.e("User", "유저 ID가 유효하지 않음")
            return
        }

        fetchAllExhibitions()

        searchBtn.setOnClickListener {
            val query = searchInput.text.toString().trim()
            if (query.isNotEmpty()) {
                searchExhibitions(query)
            }
        }

        cancelBtn.setOnClickListener {
            searchInput.setText("")
            fetchAllExhibitions()
        }
    }

    private fun fetchAllExhibitions() {
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

    private fun searchExhibitions(query: String) {
        RetrofitClient.instance.searchExhibitions(query)
            .enqueue(object : Callback<ExhibitionSearchResponse> {
                override fun onResponse(
                    call: Call<ExhibitionSearchResponse>,
                    response: Response<ExhibitionSearchResponse>
                ) {
                    val exhibitions = response.body()?.results?.map {
                        Exhibition(
                            id = it.id,
                            title = it.title,
                            period = "${it.start_date} ~ ${it.end_date}",
                            location = it.location,
                            imageUrl = it.image_url
                        )
                    } ?: emptyList()

                    exhibitionAdapter = ExhibitionAdapter(
                        exhibitions,
                        { exhibition ->
                            val intent =
                                Intent(this@ArtRecommendationActivity, ReviewActivity::class.java)
                            intent.putExtra("EXHIBITION_ID", exhibition.id)
                            startActivity(intent)
                        },
                        userId
                    )
                    recyclerView.adapter = exhibitionAdapter
                }

                override fun onFailure(call: Call<ExhibitionSearchResponse>, t: Throwable) {
                    Log.e("Search", "검색 실패: ${t.message}")
                }
            })

    }
}











