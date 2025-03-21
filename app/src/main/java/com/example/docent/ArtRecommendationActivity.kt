package com.example.docent
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class ArtRecommendationActivity : AppCompatActivity() {
    private lateinit var exhibitionAdapter: ExhibitionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_art_recommendation)

        // 기본 전시회 데이터 (서버 데이터가 없을 경우 보이는 값)
        val defaultExhibitions = listOf(
            Exhibition("최서희 : 용기있는 자", "2025.03.20 ~ 2025.04.01", "청년베프"),
            Exhibition("김재범 개인전", "2025.03.17 ~ 2025.03.30", "어바웃 프로젝트라운지"),
            Exhibition("김정인 : All lives are m...", "2025.03.19 ~ 2025.04.08", "충무로갤러리"),
            Exhibition("백승주 개인전", "2025.03.19 ~ 2025.03.24", "인영갤러리")
        )

        // RecyclerView 설정
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        exhibitionAdapter = ExhibitionAdapter(defaultExhibitions)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = exhibitionAdapter

        // 🚀 서버에서 데이터 가져와서 업데이트 (예시)
        fetchExhibitionsFromServer()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val mypageImage = findViewById<ImageView>(R.id.mypage)
        mypageImage.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
            finish()
        }
        // 메인 화면으로 돌아가는 버튼 초기화 및 클릭 리스너 설정
        /**/
        val CameraButton = findViewById<ImageView>(R.id.Camera_Button)
        CameraButton.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        // 커뮤니티 화면으로 이동
        val comuButton = findViewById<ImageView>(R.id.Commu_Button)
        comuButton.setOnClickListener {
            val intent = Intent(this, communityActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetchExhibitionsFromServer() {
        // 여기에서 서버 데이터 불러오기 (예제: 서버 데이터 받아서 업데이트)
        val serverExhibitions = listOf(
            Exhibition("새로운 전시회", "2025.04.10 ~ 2025.04.30", "서울미술관"),
            Exhibition("현대미술 특별전", "2025.05.01 ~ 2025.06.15", "국립현대미술관"),
            Exhibition("최서희 : 용기있는 자", "2025.03.20 ~ 2025.04.01", "청년베프"),
            Exhibition("김재범 개인전", "2025.03.17 ~ 2025.03.30", "어바웃 프로젝트라운지"),
            Exhibition("김정인 : All lives are m...", "2025.03.19 ~ 2025.04.08", "충무로갤러리"),
            Exhibition("백승주 개인전", "2025.03.19 ~ 2025.03.24", "인영갤러리")

        )

        // 데이터를 업데이트 (기본 데이터 → 서버 데이터)
        exhibitionAdapter.updateData(serverExhibitions)
    }

}
