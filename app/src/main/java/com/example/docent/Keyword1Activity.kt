package com.example.docent

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.docent.databinding.ActivityKeyword1Binding
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.ceil
import kotlin.math.min

class Keyword1Activity : AppCompatActivity() {

    private lateinit var binding: ActivityKeyword1Binding
    private lateinit var imageViews: List<ImageView>

    private var artworks: List<Artwork> = emptyList()
    private val selectedArtworkIds = mutableSetOf<Int>()

    private var pageIndex = 0
    private val pageSize = 4
    private var totalPages = 0

    /// 프리페치 임계값(멤버 변수로 선언되어 있어야 합니다!)
    private var targetCount: Int = 12

    // 프리페치 중복 방지 플래그
    private var prefetchFired = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeyword1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // 4개 이미지 뷰
        imageViews = listOf(binding.artwork1, binding.artwork2, binding.artwork3, binding.artwork4)

        // 버튼 리스너
        binding.prev.setOnClickListener {
            if (pageIndex > 0) {
                pageIndex -= 1
                bindPage(pageIndex)
            }
        }
        binding.next6.setOnClickListener {
            // 마지막 페이지면 "완료" 로직 수행, 아니면 다음 페이지
            if (pageIndex == totalPages - 1) {
                submitPreference()
            } else {
                pageIndex += 1
                bindPage(pageIndex)
            }
        }

        // 최초 데이터 로드
        loadRandomArtworks()
    }

    private fun loadRandomArtworks() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("accessToken", null)

        if (token == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val authHeader = "Bearer $token" // 반드시 "Bearer " 포함 (기존 코드와 동일) :contentReference[oaicite:5]{index=5}

        // 서버에서 랜덤 작품 리스트 수신 (기존 API 그대로 사용) :contentReference[oaicite:6]{index=6}
        RetrofitClient.instance.getRandomArtworks(authHeader).enqueue(object : Callback<List<Artwork>> {
            override fun onResponse(call: Call<List<Artwork>>, response: Response<List<Artwork>>) {
                if (response.isSuccessful) {
                    val all = response.body().orEmpty()
                    if (all.isEmpty()) {
                        Toast.makeText(this@Keyword1Activity, "작품이 충분하지 않습니다.", Toast.LENGTH_SHORT).show()
                        return
                    }

                    // 총 12장만 사용(12장 미만이면 가능한 만큼) — 멤버 변수에 저장
                    this@Keyword1Activity.targetCount = min(12, all.size)
                    artworks = all.take(this@Keyword1Activity.targetCount)

                    // 총 페이지 계산 (4장/페이지)
                    totalPages = ceil(this@Keyword1Activity.targetCount / pageSize.toDouble())
                        .toInt()
                        .coerceAtLeast(1)

                    // 첫 페이지 바인딩
                    pageIndex = 0
                    bindPage(pageIndex)
                } else {
                    Log.e("Keyword1", "응답 실패: ${response.code()}")
                    Toast.makeText(this@Keyword1Activity, "서버 응답 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Artwork>>, t: Throwable) {
                Log.e("Keyword1", "작품 불러오기 실패: ${t.message}")
                Toast.makeText(this@Keyword1Activity, "작품 불러오기 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun bindPage(page: Int) {
        // 페이지 인디케이터 갱신
        binding.tvPageIndicator.text = "${page + 1} / $totalPages"

        // 버튼 상태/아이콘 갱신
        binding.prev.isEnabled = page > 0
        binding.prev.alpha = if (page > 0) 1.0f else 0.3f

        val isLast = page == totalPages - 1

        // 마지막 페이지면 '완료' 체크 아이콘, 아니면 '다음' 화살표
        val nextIcon = if (isLast) R.drawable.ic_chevron_right_24 else R.drawable.ic_chevron_right_24
        binding.next6.setImageResource(nextIcon)

        // 현재 페이지의 작품 4장 구간
        val start = page * pageSize
        val end = min(start + pageSize, artworks.size)
        val slice = artworks.subList(start, end)

        // 4개 뷰 바인딩
        for (i in imageViews.indices) {
            val iv = imageViews[i]
            if (i < slice.size) {
                val art = slice[i]
                iv.tag = art.id // 선택 토글 시 식별용
                iv.visibility = ImageView.VISIBLE

                Glide.with(this).load(art.image_url).into(iv)

                // 선택상태 반영(반투명)
                iv.alpha = if (selectedArtworkIds.contains(art.id)) 0.5f else 1.0f

                iv.setOnClickListener {
                    toggleSelection(iv, art.id)
                }
            } else {
                iv.tag = null
                iv.setImageDrawable(null)
                iv.visibility = ImageView.INVISIBLE
            }
        }
    }

    private fun toggleSelection(iv: ImageView, artworkId: Int) {
        if (selectedArtworkIds.contains(artworkId)) {
            selectedArtworkIds.remove(artworkId)
            iv.alpha = 1.0f
        } else {
            selectedArtworkIds.add(artworkId)
            iv.alpha = 0.5f
        }
        //  12장(또는 targetCount) 모두 선택되는 "그 순간" 백그라운드 프리페치
        if (!prefetchFired && selectedArtworkIds.size >= targetCount) {
            prefetchFired = true
            prefetchAnalyze()
        }
    }

    private fun submitPreference() {
        if (selectedArtworkIds.isEmpty()) {
            Toast.makeText(this, "하나 이상의 작품을 선택하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("accessToken", null)
        if (token == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = prefs.getInt("user_id", -1)
        if (userId == -1) {
            Toast.makeText(this, "유저 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val authHeader = "Bearer $token"

        val preferenceRequest = PreferenceRequest(
            user_id = userId.toString(),
            artwork_ids = selectedArtworkIds.toList()
        )

        // 마지막에 한 번만 분석 요청 (기존 analyzePreference 호출을 그대로 활용) :contentReference[oaicite:7]{index=7}
        RetrofitClient.instance.analyzePreference(authHeader, preferenceRequest)
            .enqueue(object : Callback<PreferenceResponse> {
                override fun onResponse(
                    call: Call<PreferenceResponse>,
                    response: Response<PreferenceResponse>
                ) {
                    if (response.isSuccessful) {
                        // 최초 로그인 플래그 해제 등 기존 후속 처리 유지
                        prefs.edit().putBoolean("isFirstLogin", false).apply()

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
                        intent.putIntegerArrayListExtra(
                            "selected_artwork_ids",
                            ArrayList(selectedArtworkIds.toList())
                        )
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
    }

    // 프리페치: 12장 선택 완료되는 즉시 백그라운드로 분석 → 캐시 저장
    private fun prefetchAnalyze() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("accessToken", null) ?: return
        val userId = prefs.getInt("user_id", -1)
        if (userId == -1) return

        val authHeader = "Bearer $token"
        val req = PreferenceRequest(
            user_id = userId.toString(),
            artwork_ids = selectedArtworkIds.toList()
        )

        RetrofitClient.instance.analyzePreference(authHeader, req)
            .enqueue(object : Callback<PreferenceResponse> {
                override fun onResponse(call: Call<PreferenceResponse>, response: Response<PreferenceResponse>) {
                    if (!response.isSuccessful) return
                    val body = response.body() ?: return

                    val movements = (body.movements ?: emptyList()).map { it.name to it.count }
                    val moods     = (body.moods ?: emptyList()).map { it.name to it.count }
                    savePreferenceCountsToCache(movements, moods)
                    Log.d("Prefetch", "분석 결과 캐시 저장 완료(선택 ${selectedArtworkIds.size}건)")
                }

                override fun onFailure(call: Call<PreferenceResponse>, t: Throwable) {
                    Log.e("Prefetch", "분석 프리페치 실패: ${t.message}")
                }
            })
    }

    // Mypage와 동일 포맷으로 저장 (SWR 즉시 표시)
    private fun savePreferenceCountsToCache(
        movements: List<Pair<String, Int>>,
        moods: List<Pair<String, Int>>
    ) {
        val prefs = getSharedPreferences("mypage_prefs", MODE_PRIVATE)

        fun List<Pair<String, Int>>.toJsonArray(): JSONArray =
            JSONArray().apply {
                for ((name, count) in this@toJsonArray) {
                    put(JSONObject().apply {
                        put("name", name)
                        put("count", count)
                    })
                }
            }

        val root = JSONObject().apply {
            put("movements", movements.toJsonArray())
            put("moods", moods.toJsonArray())
        }
        prefs.edit().putString("pref_counts_json", root.toString()).apply()
    }

}
