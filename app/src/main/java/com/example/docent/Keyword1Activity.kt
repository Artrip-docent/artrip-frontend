package com.example.docent

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.docent.databinding.ActivityKeyword1Binding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import okhttp3.ResponseBody

class Keyword1Activity : AppCompatActivity() {

    private lateinit var binding: ActivityKeyword1Binding
    private lateinit var imageViews: List<ImageView>
    private lateinit var artworks: List<Artwork>
    private val selectedArtworkIds = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeyword1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // ImageView 4개 연결
        imageViews = listOf(
            binding.artwork1, binding.artwork2, binding.artwork3, binding.artwork4
        )

        // 이미지 로드
        loadRandomArtworks()

        // 다음 버튼 눌렀을 때 서버 전송
        binding.next6.setOnClickListener {
            if (selectedArtworkIds.isNotEmpty()) {
                val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                val token = prefs.getString("accessToken", null)

                if (token == null) {
                    Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val authHeader = "Bearer $token"

                val userId = prefs.getInt("user_id", -1)
                if (userId == -1) {
                    Toast.makeText(this, "유저 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val preferenceRequest = PreferenceRequest(
                    user_id = userId.toString(),
                    artwork_ids = selectedArtworkIds
                )

                RetrofitClient.instance.analyzePreference(authHeader, preferenceRequest)
                    .enqueue(object : Callback<PreferenceResponse> {
                        override fun onResponse(
                            call: Call<PreferenceResponse>,
                            response: Response<PreferenceResponse>
                        ) {
                            if (response.isSuccessful) {

                                RetrofitClient.instance.markPreferenceComplete(authHeader)
                                    .enqueue(object : Callback<ResponseBody> {
                                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                                            Log.d("Preference", "분석 완료 처리됨")
                                        }

                                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                            Log.e("Preference", "분석 완료 처리 실패: ${t.message}")
                                        }
                                    })

                                val intent = Intent(this@Keyword1Activity, MypageActivity::class.java)
                                intent.putIntegerArrayListExtra("selected_artwork_ids", ArrayList(selectedArtworkIds))
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this@Keyword1Activity, "서버 응답 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<PreferenceResponse>, t: Throwable) {
                            Toast.makeText(this@Keyword1Activity, "서버 연결 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            } else {
                Toast.makeText(this@Keyword1Activity, "하나 이상의 작품을 선택하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadRandomArtworks() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("accessToken", null)

        if (token == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val authHeader = "Bearer $token"  // "Bearer " 접두어 반드시 포함

        RetrofitClient.instance.getRandomArtworks(authHeader).enqueue(object : Callback<List<Artwork>> {
            override fun onResponse(
                call: Call<List<Artwork>>,
                response: Response<List<Artwork>>
            ) {
                if (response.isSuccessful) {
                    artworks = response.body() ?: emptyList()

                    if (artworks.size >= 4) {
                        for (i in 0 until 4) {
                            val imageUrl = artworks[i].image_url
                            Glide.with(this@Keyword1Activity)
                                .load(imageUrl)
                                .into(imageViews[i])

                            imageViews[i].setOnClickListener {
                                val artworkId = artworks[i].id

                                if (selectedArtworkIds.contains(artworkId)) {
                                    selectedArtworkIds.remove(artworkId)
                                    imageViews[i].alpha = 1.0f // 선택 해제 시 원래 투명도
                                } else {
                                    selectedArtworkIds.add(artworkId)
                                    imageViews[i].alpha = 0.5f // 선택 시 반투명 처리
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this@Keyword1Activity, "작품이 충분하지 않습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("Keyword1", "응답 실패: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Artwork>>, t: Throwable) {
                Log.e("Keyword1", "작품 불러오기 실패: ${t.message}")
            }
        })
    }
}
