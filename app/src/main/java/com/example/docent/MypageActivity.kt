package com.example.docent

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.docent.PreferenceRequest
import com.example.docent.PreferenceResponse


class MypageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        val pieChart: PieChart = findViewById(R.id.pieChart)



        // PieChart 기본 설정
        pieChart.description.isEnabled = false
        pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setCenterText("")
        pieChart.setCenterTextSize(18f)
        pieChart.legend.isEnabled = false
        pieChart.isDrawHoleEnabled = false

        pieChart.setNoDataText("")         // 텍스트 없애기
        pieChart.setNoDataTextColor(Color.TRANSPARENT) // 색상도 투명하게


        val selectedArtworkIds = intent.getIntegerArrayListExtra("selected_artwork_ids") ?: listOf()


        val preferenceRequest = PreferenceRequest(
            user_id = "user_123", // 고정된 ID 또는 로그인 연동 시 유동적으로
            artwork_ids = selectedArtworkIds
        )

        RetrofitClient.instance.analyzePreference(preferenceRequest)
            .enqueue(object : Callback<PreferenceResponse> {
                override fun onResponse(
                    call: Call<PreferenceResponse>,
                    response: Response<PreferenceResponse>
                ) {
                    if (response.isSuccessful) {
                        val result = response.body()
                        if (result != null) {
                            val entries = mutableListOf<PieEntry>()
                            result.topTags.forEach { tag ->
                                entries.add(PieEntry(1f, tag))  // 퍼센트 대신 동일 값으로 설정
                            }
                            // 결과 저장
                            saveTagsToPreferences(result.topTags)
                            showPieChart(pieChart, entries)
                        } else {
                            // 응답은 성공했지만 결과가 비어있을 경우 → 저장된 태그 불러오기
                            loadAndShowSavedTags(pieChart)
                        }

                    } else {
                        // 응답은 성공했지만 결과가 비어있을 경우 -> 저장된 태그 불러오기
                        Log.e("Mypage", "서버 응답 오류: ${response.code()}")
                        loadAndShowSavedTags(pieChart)
                    }
                }

                override fun onFailure(call: Call<PreferenceResponse>, t: Throwable) {
                    Log.e("Mypage", "분석 실패: ${t.message}")
                    // ✅ 서버 연결 실패 시 → 저장된 태그 불러오기
                    loadAndShowSavedTags(pieChart)
                }
            })




        //  아래는 기존 네비게이션 코드 유지
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
        }

        findViewById<ImageView>(R.id.museum).setOnClickListener {
            startActivity(Intent(this, ArtRecommendationActivity::class.java))
            finish()
        }

        findViewById<ImageView>(R.id.chat).setOnClickListener {
            startActivity(Intent(this, ChathistoryActivity::class.java))
            finish()
        }

        findViewById<ImageView>(R.id.Camera_Button).setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        findViewById<ImageView>(R.id.Commu_Button).setOnClickListener {
            startActivity(Intent(this, communityActivity::class.java))
        }

        findViewById<ImageView>(R.id.setting).setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }

        findViewById<TextView>(R.id.preferenceanalysis).setOnClickListener {
            startActivity(Intent(this, PreferenceanalysisActivity::class.java))
        }
    }

    // 🔧 PieChart 출력 함수
    private fun showPieChart(pieChart: PieChart, entries: List<PieEntry>) {
        val dataSet = PieDataSet(entries, "취향 분석")

        // ✨ 차분한 톤의 색상으로 표현
        dataSet.setColors(
            Color.rgb(179, 205, 224), // 연한 하늘색
            Color.rgb(251, 180, 174), // 부드러운 연분홍
            Color.rgb(204, 235, 197), // 연한 민트
            Color.rgb(222, 203, 228), // 연보라
            Color.rgb(254, 217, 166)  // 살구색
        )

        val data = PieData(dataSet)
        data.setDrawValues(false)
        pieChart.data = data
        pieChart.invalidate()
    }

    // 🔐 저장 함수
    private fun saveTagsToPreferences(tags: List<String>) {
        val prefs = getSharedPreferences("preference_tags", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("tags", tags.joinToString(","))
        editor.apply()
    }

    // 📤 불러오기 함수
    private fun loadTagsFromPreferences(): List<String> {
        val prefs = getSharedPreferences("preference_tags", MODE_PRIVATE)
        val savedString = prefs.getString("tags", "") ?: ""
        return if (savedString.isNotEmpty()) savedString.split(",") else emptyList()
    }

    // 🎯 저장된 태그 불러와서 원형 그래프에 보여주는 함수
    private fun loadAndShowSavedTags(pieChart: PieChart) {
        val savedTags = loadTagsFromPreferences()
        if (savedTags.isNotEmpty()) {
            val entries = savedTags.map { PieEntry(1f, it) } // 퍼센트 없이 동일한 값
            showPieChart(pieChart, entries)
        }
    }




}