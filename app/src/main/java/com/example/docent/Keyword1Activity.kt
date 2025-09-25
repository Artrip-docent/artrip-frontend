package com.example.docent

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.docent.databinding.ActivityKeyword1Binding
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.ceil
import kotlin.math.min
import android.view.View


class Keyword1Activity : AppCompatActivity() {

    private lateinit var binding: ActivityKeyword1Binding
    private lateinit var imageViews: List<ImageView>

    private var artworks: MutableList<Artwork> = mutableListOf()
    private val selectedArtworkIds = linkedSetOf<Int>()

    private var pageIndex = 0
    private val pageSize = 4
    private var totalPages = 0
    private val targetCount = 12 // 항상 3페이지 보장

    private var prefetchFired = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeyword1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        imageViews = listOf(binding.artwork1, binding.artwork2, binding.artwork3, binding.artwork4)

        binding.prev.setOnClickListener {
            if (pageIndex > 0) {
                pageIndex -= 1
                bindPage(pageIndex)
            }
        }
        binding.next6.setOnClickListener {
            if (pageIndex == totalPages - 1) {
                submitPreferenceOptimistic()
            } else {
                pageIndex += 1
                bindPage(pageIndex)
            }
        }

        loadRandomArtworks()
    }
//    override fun onStop() {
//        super.onStop()
//        // 안전: 화면 나갈 때 현재 바인딩된 요청들 취소
//        imageViews.forEach { Glide.with(this).clear(it) }
//    }

    private fun loadRandomArtworks() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("accessToken", null)
        if (token == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val authHeader = "Bearer $token"

        RetrofitClient.instance.getRandomArtworks(authHeader)
            .enqueue(object : Callback<List<Artwork>> {
                override fun onResponse(call: Call<List<Artwork>>, response: Response<List<Artwork>>) {
                    if (!response.isSuccessful) {
                        Log.e("Keyword1", "응답 실패: ${response.code()}")
                        Toast.makeText(this@Keyword1Activity, "서버 응답 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                        return
                    }
                    val all = response.body().orEmpty()
                    if (all.isEmpty()) {
                        Toast.makeText(this@Keyword1Activity, "작품이 충분하지 않습니다.", Toast.LENGTH_SHORT).show()
                        return
                    }

                    // 12장 미만이면 순환 채움으로 12장 보장 (항상 4×3)
                    val filled = mutableListOf<Artwork>()
                    var idx = 0
                    while (filled.size < targetCount) {
                        filled += all[idx % all.size]
                        idx++
                    }
                    artworks = filled
                    totalPages = ceil(targetCount / pageSize.toDouble()).toInt() // = 3
                    pageIndex = 0
                    bindPage(pageIndex)
                }

                override fun onFailure(call: Call<List<Artwork>>, t: Throwable) {
                    Log.e("Keyword1", "작품 불러오기 실패: ${t.message}")
                    Toast.makeText(this@Keyword1Activity, "작품 불러오기 실패: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun bindPage(page: Int) {
        try {
            binding.tvPageIndicator.text = "${page + 1} / $totalPages"

            binding.prev.isEnabled = page > 0
            binding.prev.alpha = if (page > 0) 1.0f else 0.3f

            val start = page * pageSize
            val end = start + pageSize            // 항상 4칸을 정확히 자름 (artworks는 12장 보장)
            val slice = artworks.subList(start, end)

            val ph = ColorDrawable(0xFFE0E0E0.toInt())

            for (i in imageViews.indices) {
                val iv = imageViews[i]

                // 이전 요청 취소 + 기본 상태 초기화
                Glide.with(iv).clear(iv)
                iv.visibility = View.VISIBLE       // 혹시 INVISIBLE로 남아있던 셀을 확실히 보이게
                iv.setImageDrawable(ph)

                // 현재 칸 데이터 (항상 4개 보장)
                val art = slice[i]
                iv.tag = art.id

                val selected = selectedArtworkIds.contains(art.id)
                iv.alpha = if (selected) 0.5f else 1f
                iv.isClickable = true

                // 실패 1회 재시도 포함 로더
                loadImageWithRetry(iv, art.image_url, selected)

                iv.setOnClickListener { toggleSelection(iv, art.id) }
            }

            preloadPageImages(page + 1)
            preloadPageImages(page - 1)
        } catch (e: Throwable) {
            Log.e("Keyword1", "bindPage error: ${e.message}", e)
        }
    }

    private fun preloadPageImages(page: Int) {
        if (page < 0 || page >= totalPages) return
        val nStart = page * pageSize
        val nEnd = min(nStart + pageSize, artworks.size)
        for (j in nStart until nEnd) {
            // View context를 못 쓰니 Activity context 사용은 OK
            Glide.with(this)
                .load(artworks[j].image_url)
                .thumbnail(0.25f)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .preload()
        }
    }

    // 실패 시 1회 자동 재시도 + Activity 상태 가드 + with(iv)
    private fun loadImageWithRetry(
        iv: ImageView,
        url: String?,                 // nullable
        selected: Boolean,
        retry: Boolean = true
    ) {
        val ph = ColorDrawable(0xFFE0E0E0.toInt())

        // 상태 가드
        if (isFinishing || isDestroyed || !iv.isAttachedToWindow) {
            iv.setImageDrawable(ph)
            iv.alpha = if (selected) 0.5f else 1.0f
            return
        }

        // null/blank URL → 즉시 플레이스홀더, 재시도 안 함
        val safeUrl = url?.takeIf { it.isNotBlank() }
        if (safeUrl == null) {
            iv.setImageDrawable(ph)
            iv.alpha = if (selected) 0.5f else 1.0f
            Log.e("Glide", "이미지 URL이 비어있음. 재시도 생략")
            return
        }

        // ---- 워치독: 지정 시간 후에도 placeholder면 강제 재시도 (한 번만) ----
        // 너무 오래 대기하는 요청을 깨우는 역할
        val watchdogDelayMs = 5000L
        val watchdog = Runnable {
            if (!iv.isAttachedToWindow || isFinishing || isDestroyed) return@Runnable
            val stillPlaceholder = (iv.drawable == null) || (iv.drawable is ColorDrawable)
            if (stillPlaceholder) {
                Log.w("Glide", "워치독 트리거 → 강제 재시도: $safeUrl")
                // 강제 한 번 더(이미 retry를 썼다면 false로 들어감)
                loadImageWithRetry(iv, safeUrl, selected, false)
            }
        }
        iv.removeCallbacks(watchdog)
        iv.postDelayed(watchdog, watchdogDelayMs)

        // ---- 실제 로딩 ----
        Glide.with(iv)
            .load(safeUrl)
            .thumbnail(0.25f)
            .centerCrop()
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .placeholder(ph)
            .error(ph)
            .fallback(ph)
            .timeout(15_000) // OkHttp 통합 시 유효(MyGlideModule.kt 붙여놨음)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean
                ): Boolean {
                    iv.removeCallbacks(watchdog)
                    Log.e("Glide", "로드 실패: $model, ${e?.rootCauses?.firstOrNull()?.message}")
                    if (retry && iv.isAttachedToWindow) {
                        iv.postDelayed({ loadImageWithRetry(iv, safeUrl, selected, false) }, 1000)
                    }
                    return false
                }
                override fun onResourceReady(
                    resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean
                ): Boolean {
                    iv.removeCallbacks(watchdog)
                    return false
                }
            })
            .into(iv)

        iv.alpha = if (selected) 0.5f else 1.0f
    }

    private fun toggleSelection(iv: ImageView, artworkId: Int) {
        if (selectedArtworkIds.remove(artworkId)) {
            iv.alpha = 1.0f
        } else {
            selectedArtworkIds.add(artworkId)
            iv.alpha = 0.5f
        }
        // 3장 이상(= 각 페이지 최소 1장씩) 고르면 1회 프리페치
        if (!prefetchFired && hasOneSelectionPerPage()) {
            prefetchFired = true
            prefetchAnalyze()
        }
    }

    // 각 페이지에서 최소 1개씩 선택했는지 확인
    private fun hasOneSelectionPerPage(): Boolean {
        if (selectedArtworkIds.size < 3) return false
        val covered = BooleanArray(totalPages) { false }
        val idToFirstIndex = HashMap<Int, Int>().apply {
            artworks.forEachIndexed { idx, art -> putIfAbsent(art.id, idx) }
        }
        for (id in selectedArtworkIds) {
            val idx = idToFirstIndex[id] ?: continue
            val p = idx / pageSize
            if (p in 0 until totalPages) covered[p] = true
        }
        return covered.all { it }
    }

    // 마지막 페이지에서 즉시 마이페이지로 이동 + 백그라운드 분석
    private fun submitPreferenceOptimistic() {
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

        // 1) 즉시 마이페이지로 이동 (캐시가 있으면 먼저 그려짐)
        Intent(this, MypageActivity::class.java).also {
            it.putIntegerArrayListExtra("selected_artwork_ids", ArrayList(selectedArtworkIds))
            startActivity(it)
        }
        finish()

        // 2) 백그라운드 분석 요청 → 캐시 저장 → 완료 마킹
        val authHeader = "Bearer $token"
        val req = PreferenceRequest(user_id = userId.toString(), artwork_ids = selectedArtworkIds.toList())

        RetrofitClient.instance.analyzePreference(authHeader, req)
            .enqueue(object : Callback<PreferenceResponse> {
                override fun onResponse(call: Call<PreferenceResponse>, response: Response<PreferenceResponse>) {
                    if (!response.isSuccessful) return
                    val body = response.body() ?: return
                    val movements = (body.movements ?: emptyList()).map { it.name to it.count }
                    val moods = (body.moods ?: emptyList()).map { it.name to it.count }
                    savePreferenceCountsToCache(movements, moods)

                    RetrofitClient.instance.markPreferenceComplete(authHeader)
                        .enqueue(object : Callback<ResponseBody> {
                            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) { /* no-op */ }
                            override fun onFailure(call: Call<ResponseBody>, t: Throwable) { /* no-op */ }
                        })
                }
                override fun onFailure(call: Call<PreferenceResponse>, t: Throwable) { /* 마이페이지는 이미 열림 */ }
            })
    }

    // 선택 3장 충족 시 프리페치 분석 → 캐시에 저장 (SWR)
    private fun prefetchAnalyze() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("accessToken", null) ?: return
        val userId = prefs.getInt("user_id", -1)
        if (userId == -1) return

        val authHeader = "Bearer $token"
        val req = PreferenceRequest(user_id = userId.toString(), artwork_ids = selectedArtworkIds.toList())

        RetrofitClient.instance.analyzePreference(authHeader, req)
            .enqueue(object : Callback<PreferenceResponse> {
                override fun onResponse(call: Call<PreferenceResponse>, response: Response<PreferenceResponse>) {
                    val body = response.body() ?: return
                    val movements = (body.movements ?: emptyList()).map { it.name to it.count }
                    val moods = (body.moods ?: emptyList()).map { it.name to it.count }
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
