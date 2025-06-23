package com.example.docent

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

data class UserInfoResponse(
    val email: String,
    val nickname: String,
    val profile_image: String
)

class MypageActivity : AppCompatActivity() {

    private lateinit var imageViewProfile: ImageView
    private lateinit var textViewNickname: TextView
    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        imageViewProfile = findViewById(R.id.imageViewProfile)
        textViewNickname = findViewById(R.id.textViewNickname)
        pieChart = findViewById(R.id.pieChart)

        loadUserProfile()
        setupPieChart()
        requestPreferenceAnalysis()
        setupNavigation()
    }

    private fun getToken(): String {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        return prefs.getString("accessToken", "") ?: ""
    }

    private fun getUserId(): Int {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        return prefs.getInt("user_id", -1)
    }

    private fun loadUserProfile() {
        val token = getToken()
        if (token.isEmpty()) {
            // 로그인 필요 시 로그인 화면 이동
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val authHeader = "Bearer $token"

        RetrofitClient.instance.getUserInfo(authHeader)
            .enqueue(object : Callback<UserInfoResponse> {
                override fun onResponse(call: Call<UserInfoResponse>, response: Response<UserInfoResponse>) {
                    if (response.isSuccessful) {
                        val userInfo = response.body()
                        if (userInfo != null) {
                            textViewNickname.text = userInfo.nickname
                            Glide.with(this@MypageActivity)
                                .load(userInfo.profile_image)
                                .placeholder(R.drawable.ic_profile)
                                .error(R.drawable.ic_profile)
                                .into(imageViewProfile)
                        }
                    } else {
                        Log.e("Mypage", "프로필 로드 실패 코드: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<UserInfoResponse>, t: Throwable) {
                    Log.e("Mypage", "프로필 요청 실패: ${t.message}")
                }
            })
    }

    private fun setupPieChart() {
        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setCenterText("")
        pieChart.setCenterTextSize(18f)
        pieChart.legend.isEnabled = false
        pieChart.isDrawHoleEnabled = false
        pieChart.setNoDataText("")
        pieChart.setNoDataTextColor(Color.TRANSPARENT)
    }

    private fun requestPreferenceAnalysis() {
        val token = getToken()
        val userId = getUserId()

        if (token.isEmpty() || userId == -1) {
            Log.e("Mypage", "토큰이나 사용자 ID가 없습니다.")
            return
        }

        val authHeader = "Bearer $token"

        // 저장된 사용자 선택 작품 id를 가져와서 요청에 넣거나, 빈 리스트 전달 가능
        val selectedArtworkIds = intent.getIntegerArrayListExtra("selected_artwork_ids") ?: listOf()

        val preferenceRequest = PreferenceRequest(
            user_id = userId.toString(),  // 실제 유저 아이디로
            artwork_ids = selectedArtworkIds
        )

        RetrofitClient.instance.analyzePreference(authHeader, preferenceRequest)
            .enqueue(object : Callback<PreferenceResponse> {
                override fun onResponse(call: Call<PreferenceResponse>, response: Response<PreferenceResponse>) {
                    if (response.isSuccessful) {
                        val result = response.body()
                        if (result != null) {
                            val entries = mutableListOf<PieEntry>()
                            result.topTags.forEach { tag ->
                                entries.add(PieEntry(1f, tag))
                            }
                            saveTagsToPreferences(result.topTags)
                            showPieChart(entries)
                        } else {
                            loadAndShowSavedTags()
                        }
                    } else {
                        Log.e("Mypage", "취향 분석 서버 오류 코드: ${response.code()}")
                        loadAndShowSavedTags()
                    }
                }

                override fun onFailure(call: Call<PreferenceResponse>, t: Throwable) {
                    Log.e("Mypage", "취향 분석 실패: ${t.message}")
                    loadAndShowSavedTags()
                }
            })
    }

    private fun showPieChart(entries: List<PieEntry>) {
        val dataSet = PieDataSet(entries, "취향 분석")
        dataSet.setColors(
            Color.rgb(179, 205, 224),
            Color.rgb(251, 180, 174),
            Color.rgb(204, 235, 197),
            Color.rgb(222, 203, 228),
            Color.rgb(254, 217, 166)
        )
        val data = PieData(dataSet)
        data.setDrawValues(false)
        pieChart.data = data
        pieChart.invalidate()
    }

    private fun saveTagsToPreferences(tags: List<String>) {
        val prefs = getSharedPreferences("preference_tags", MODE_PRIVATE)
        prefs.edit().putString("tags", tags.joinToString(",")).apply()
    }

    private fun loadTagsFromPreferences(): List<String> {
        val prefs = getSharedPreferences("preference_tags", MODE_PRIVATE)
        val savedString = prefs.getString("tags", "") ?: ""
        return if (savedString.isNotEmpty()) savedString.split(",") else emptyList()
    }

    private fun loadAndShowSavedTags() {
        val savedTags = loadTagsFromPreferences()
        if (savedTags.isNotEmpty()) {
            val entries = savedTags.map { PieEntry(1f, it) }
            showPieChart(entries)
        }
    }

    private fun setupNavigation() {
        findViewById<ImageView>(R.id.ticket).setOnClickListener {
            startActivity(Intent(this, exhibitionviewingActivity::class.java))
            finish()
        }

        findViewById<ImageView>(R.id.heart).setOnClickListener {
            startActivity(Intent(this, GoodexhibitionActivity::class.java))
            finish()
        }

        findViewById<TextView>(R.id.logout).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        findViewById<ImageView>(R.id.nav_home).setOnClickListener {
            startActivity(Intent(this, ArtRecommendationActivity::class.java))
            finish()
        }

        findViewById<ImageView>(R.id.profile_edit).setOnClickListener {
            startActivity(Intent(this, ProfileEditActivity::class.java))
            finish()
        }

        findViewById<ImageView>(R.id.nav_camera).setOnClickListener {
            startActivity(Intent(this, ExhibitionSelectActivity::class.java))
        }

        findViewById<TextView>(R.id.preferenceanalysis).setOnClickListener {
            startActivity(Intent(this, PreferenceanalysisActivity::class.java))
        }
    }
}
