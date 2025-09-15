package com.example.docent

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.flexbox.FlexboxLayout
import android.view.View
import android.widget.LinearLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import org.json.JSONArray
import org.json.JSONObject

data class UserInfoResponse(
    val email: String,
    val nickname: String,
    val profile_image: String
)

class MypageActivity : AppCompatActivity() {

    private lateinit var imageViewProfile: ImageView
    private lateinit var textViewNickname: TextView
    private lateinit var tvMovementSummary: TextView
    private lateinit var pieChartMovement: PieChart
    private lateinit var moodCloud: FlexboxLayout
    private lateinit var legendContainer: LinearLayout
    // 개수 조절 상수
    private val TOP_MOVEMENTS = 2   // 사조 2개
    private val TOP_MOODS = 4      // 분위기 4개

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        imageViewProfile   = findViewById(R.id.imageViewProfile)
        textViewNickname   = findViewById(R.id.textViewNickname)
        tvMovementSummary  = findViewById(R.id.tvMovementSummary)
        pieChartMovement   = findViewById(R.id.pieChartMovement)
        moodCloud          = findViewById(R.id.moodCloud)
        legendContainer   = findViewById(R.id.legendContainer)

        setupMovementChart()   // 도넛 차트 공통 스타일
        loadUserProfile()
        loadAndShowSavedTags()  // SWR: 캐시 먼저 그려두고, 이어서 네트워크로 최신값 갱신
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

    /** 도넛 차트 공통 스타일 */
    private fun setupMovementChart() {
        pieChartMovement.description.isEnabled = false
        pieChartMovement.setUsePercentValues(true)
        pieChartMovement.setEntryLabelColor(Color.BLACK)
        pieChartMovement.setEntryLabelTextSize(11f)

        // 도넛
        pieChartMovement.isDrawHoleEnabled = true
        pieChartMovement.holeRadius = 58f
        pieChartMovement.transparentCircleRadius = 62f
        pieChartMovement.setCenterText("")
        // 조각 위 텍스트는 안 보이게
        pieChartMovement.setDrawEntryLabels(false)
        // 기본 범례는 사용 안 함 (우리는 오른쪽에 별도 컨테이너 사용)
        pieChartMovement.legend.isEnabled = false
        // 좌우 여백을 동일하게(가운데 시각 정렬에 도움)
        pieChartMovement.setExtraOffsets(8f, 8f, 8f, 8f)


    }

    private fun requestPreferenceAnalysis() {
        val token = getToken()
        val userId = getUserId()
        if (token.isEmpty() || userId == -1) { /* 생략 */ return }

        val authHeader = "Bearer $token"
        val selectedArtworkIds = intent.getIntegerArrayListExtra("selected_artwork_ids") ?: emptyList<Int>()
        val preferenceRequest = PreferenceRequest(user_id = userId.toString(), artwork_ids = selectedArtworkIds)

        RetrofitClient.instance.analyzePreference(authHeader, preferenceRequest)
            .enqueue(object : Callback<PreferenceResponse> {
                override fun onResponse(call: Call<PreferenceResponse>, res: Response<PreferenceResponse>) {
                    if (res.isSuccessful) {
                        val body = res.body()
                        if (body != null) {
                            val movementPairs = (body.movements ?: emptyList()).map { it.name to it.count }
                            val moodPairs     = (body.moods ?: emptyList()).map { it.name to it.count }
                            // 새 포맷(카운트)으로 저장
                            savePreferenceCounts(movementPairs, moodPairs)
                            // 화면 그리기
                            renderPreferenceUIFromCounts(
                                topMovement = body.top_movement,
                                movementCounts = movementPairs,
                                moodCounts = moodPairs
                            )
                        } else loadAndShowSavedTags()
                    } else { loadAndShowSavedTags() }
                }
                override fun onFailure(call: Call<PreferenceResponse>, t: Throwable) {
                    loadAndShowSavedTags()
                }
            })
    }

    private fun renderPreferenceUIFromCounts(
        topMovement: String?,
        movementCounts: List<Pair<String, Int>>,
        moodCounts: List<Pair<String, Int>>
    ) {
        // 도넛에 실제로 사용할 Top2를 먼저 계산하여 문구와 시각화의 정합성 보장
        val top2 = movementCounts.sortedByDescending { it.second }.take(TOP_MOVEMENTS)

        // 최다 사조(문구용): 서버 제공값이 있으면 사용, 없으면 top2의 1등으로 보정
        val pick = if (!topMovement.isNullOrBlank()) topMovement else top2.firstOrNull()?.first

        // 상단 문구
        if (pick.isNullOrBlank()) {
            tvMovementSummary.text = "취향 분석을 시작해보세요"
            pieChartMovement.clear()
        } else {
            val text = "$topMovement 스타일을 선호하시네요!"
            val span = SpannableString(text).apply {
                val accent = Color.parseColor("#6A2CF2")
                setSpan(ForegroundColorSpan(accent), 0, pick.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                setSpan(StyleSpan(Typeface.BOLD), 0, pick.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            tvMovementSummary.text = span
            // 도넛은 상위 2개 사조만 반영
            drawMovementPieFromCounts(top2)
        }

        // 분위기 텍스트(클라우드)
        drawMoodCloudFromCounts(moodCounts)
    }

    private fun drawMovementPieFromCounts(items: List<Pair<String, Int>>) {
        if (items.isEmpty()) { pieChartMovement.clear(); legendContainer.removeAllViews(); return }

        val top = items.sortedByDescending { it.second }.take(TOP_MOVEMENTS)
        val total = top.sumOf { it.second }.toFloat()
        val entries = top.map { (label, cnt) -> PieEntry((cnt / total) * 100f, label) }

        val colors = listOf(
            Color.parseColor("#6A2CF2"),
            Color.parseColor("#B388FF"),
            Color.parseColor("#8FD3FE"),
            Color.parseColor("#FFB3B3"),
            Color.parseColor("#C6E48B"),
            Color.parseColor("#FFD166"),
            Color.parseColor("#06D6A0")
        ).take(entries.size)

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            sliceSpace = 2f
            setDrawValues(false)   // 조각 위 숫자 숨김(범례만 표시)
        }

        pieChartMovement.data = PieData(dataSet)
        pieChartMovement.invalidate()

        // 오른쪽 커스텀 범례(라벨 전부 표시)
        buildLegend(top.map { it.first }, colors)
    }

    private fun drawMoodCloudFromCounts(countsList: List<Pair<String, Int>>) {
        moodCloud.removeAllViews()
        if (countsList.isEmpty()) return

        val maxCnt = countsList.maxOf { it.second }.toFloat()
        val base = 11f
        val maxSize = 16f
        val rnd = java.util.Random()

        countsList.sortedByDescending { it.second }.take(TOP_MOODS).forEach { (word, cnt) ->
            val tv = TextView(this).apply {
                text = word
                val weight = if (maxCnt == 0f) 0f else cnt / maxCnt
                textSize = base + (maxSize - base) * weight
                setTextColor(Color.parseColor("#7E57C2"))
                alpha = 0.85f - rnd.nextFloat() * 0.2f
                setPadding(6, 4, 6, 4)
            }
            val lp = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                rightMargin = 8
                bottomMargin = 6
            }
            moodCloud.addView(tv, lp)
        }
    }


    private fun buildLegend(labels: List<String>, colors: List<Int>) {
        legendContainer.removeAllViews()
        val dp = resources.displayMetrics.density
        labels.forEachIndexed { idx, label ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, (4 * dp).toInt(), 0, (4 * dp).toInt())  // 위아래 여백 축소
            }

            // 컬러 사각형(작게)
            val swatch = View(this).apply {
                setBackgroundColor(colors[idx])
                val size = (10 * dp).toInt()                          // 10dp
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    rightMargin = (6 * dp).toInt()
                }
            }

            // 라벨(작게)
            val tv = TextView(this).apply {
                text = label
                textSize = 11f                                       // 11sp
                setTextColor(Color.BLACK)
            }

            row.addView(swatch)
            row.addView(tv)
            legendContainer.addView(row)
        }
    }



    private fun loadAndShowSavedTags() {
        val (movements, moods) = loadPreferenceCounts()
        if (movements.isEmpty() && moods.isEmpty()) return

        val topMovement = movements.maxByOrNull { it.second }?.first
        renderPreferenceUIFromCounts(
            topMovement = topMovement,
            movementCounts = movements,
            moodCounts = moods
        )
    }

    private fun savePreferenceCounts(
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

    private fun loadPreferenceCounts(): Pair<List<Pair<String, Int>>, List<Pair<String, Int>>> {
        val prefs = getSharedPreferences("mypage_prefs", MODE_PRIVATE)
        val raw = prefs.getString("pref_counts_json", null) ?: return emptyList<Pair<String, Int>>() to emptyList()

        val root = JSONObject(raw)

        fun parse(arr: JSONArray): List<Pair<String, Int>> {
            val out = ArrayList<Pair<String, Int>>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                out += o.getString("name") to o.getInt("count")
            }
            return out
        }

        val movements = parse(root.optJSONArray("movements") ?: JSONArray())
        val moods     = parse(root.optJSONArray("moods") ?: JSONArray())
        return movements to moods
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
