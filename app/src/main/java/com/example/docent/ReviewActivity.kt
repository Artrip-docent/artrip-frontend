package com.example.docent

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import retrofit2.*

class ReviewActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        // 토큰 & 유저ID 준비
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val tokenRaw = prefs.getString("accessToken", null)
        val authHeader = tokenRaw?.let { "Bearer $it" }

        val currentUserId = resolveCurrentUserId(prefs, tokenRaw)
        Log.d("ReviewActivity", "currentUserId=$currentUserId")

        recyclerView = findViewById(R.id.reviewRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ReviewAdapter(mutableListOf(), authHeader, currentUserId)
        recyclerView.adapter = adapter

        val exhibitionId = intent.getIntExtra("EXHIBITION_ID", -1)
        if (exhibitionId == -1) {
            Toast.makeText(this, "전시회 ID가 유효하지 않습니다.", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            val intent = Intent(this, ArtRecommendationActivity::class.java)
            startActivity(intent)
        }

        fetchReviews(exhibitionId)

        findViewById<ImageView>(R.id.iv_add).setOnClickListener {
            val intent = Intent(this, ReviewWriteActivity::class.java)
            intent.putExtra("EXHIBITION_ID", exhibitionId)
            startActivity(intent)
        }
    }

    private fun fetchReviews(exhibitionId: Int) {
        RetrofitClient.instance.getReviewsByExhibition(exhibitionId)
            .enqueue(object : Callback<List<Review>> {
                override fun onResponse(call: Call<List<Review>>, response: Response<List<Review>>) {
                    if (response.isSuccessful) {
                        adapter.updateData(response.body().orEmpty())
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

    /**
     * 현재 사용자 ID를 다음 순서로 해석:
     * 1) SharedPreferences "userId" 또는 "user_id"
     * 2) accessToken이 JWT라면 payload에서 user_id/id/pk 추출
     * 찾은 값은 prefs에 저장하여 이후 재사용
     */
    private fun resolveCurrentUserId(prefs: android.content.SharedPreferences, tokenRaw: String?): Int? {
        // 1) prefs에서 우선 시도
        val fromPrefs = prefs.getInt("userId", -1).takeIf { it > 0 }
            ?: prefs.getInt("user_id", -1).takeIf { it > 0 }
        if (fromPrefs != null) return fromPrefs

        // 2) JWT payload에서 추출 (SimpleJWT 등)
        val idFromJwt = tokenRaw?.let { decodeUserIdFromJwt(it) }
        if (idFromJwt != null && idFromJwt > 0) {
            prefs.edit().putInt("userId", idFromJwt).apply()
            return idFromJwt
        }
        return null
    }

    private fun decodeUserIdFromJwt(token: String): Int? {
        // 형식: header.payload.signature (Base64URL)
        val parts = token.split(".")
        if (parts.size < 2) return null
        return try {
            val payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            val payload = JSONObject(String(payloadBytes, Charsets.UTF_8))
            // 다양한 키 시도
            sequenceOf("user_id", "userId", "id", "pk")
                .mapNotNull { key -> payload.optInt(key, -1).takeIf { it > 0 } }
                .firstOrNull()
        } catch (e: Exception) {
            Log.w("ReviewActivity", "JWT decode failed: ${e.message}")
            null
        }
    }
}
