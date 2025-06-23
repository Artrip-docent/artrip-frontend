package com.example.docent

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class artworkviewingActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ArtworkGridAdapter
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artworkviewing)

        prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val userId = prefs.getInt("user_id", -1)
        val exhibitionId = intent.getIntExtra("exhibition_id", -1)

        if (userId == -1 || exhibitionId == -1) {
            Toast.makeText(this, "정보를 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        recyclerView = findViewById(R.id.artworkRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        fetchViewedArtworks(userId, exhibitionId)
    }

    private fun fetchViewedArtworks(userId: Int, exhibitionId: Int) {
        val token = prefs.getString("accessToken", null)
        if (token.isNullOrBlank()) {
            Toast.makeText(this, "로그인이 필요합니다", Toast.LENGTH_SHORT).show()
            return
        }

        val bearerToken = "Bearer $token"
        val call = RetrofitClient.instance.getViewedArtworks(bearerToken, userId, exhibitionId)

        call.enqueue(object : Callback<List<Artwork>> {
            override fun onResponse(call: Call<List<Artwork>>, response: Response<List<Artwork>>) {
                if (response.isSuccessful && response.body() != null) {
                    val artworks = response.body()!!
                    adapter = ArtworkGridAdapter(artworks, exhibitionId)
                    recyclerView.adapter = adapter
                } else {
                    Toast.makeText(this@artworkviewingActivity, "작품을 불러오지 못했습니다", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Artwork>>, t: Throwable) {
                t.printStackTrace()
                Toast.makeText(this@artworkviewingActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
            }
        })
    }
}