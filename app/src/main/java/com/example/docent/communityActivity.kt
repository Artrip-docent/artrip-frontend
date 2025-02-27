package com.example.docent

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class communityActivity : AppCompatActivity(), ArtworkAdapter.OnItemClickListener {

    // 좋아요한 작품 리스트 데이터
    val artworks = listOf(
        listviewModel("고흐", "『아를의 별이 빛나는 밤』"),
        listviewModel("다빈치", "『모나리자』"),
        listviewModel("밀레", "『이삭줍는 여인들』"),
        listviewModel("AAAA", "Aaaa"),
        listviewModel("BBBB", "Bbbb"),
        listviewModel("CCCC", "Cccc"),
        listviewModel("DDDD", "dddd"),
        listviewModel("EEEE", "eeee"),
        listviewModel("FFFF", "ffff"),
        listviewModel("GGGG", "gggg"),
        listviewModel("HHHH", "hhhh")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_community)

        // 시스템 바 인셋 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        // RecyclerView 초기화 및 레이아웃 매니저 설정
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // 어댑터에 this를 전달하여 클릭 리스너 연결
        val adapter = ArtworkAdapter(artworks, this)
        recyclerView.adapter = adapter


        val cameraButton = findViewById<ImageView>(R.id.Camera_Button)
        cameraButton.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
        // 작품추천화면으로 이동
        val frameButton = findViewById<ImageView>(R.id.Frame_Button)
        frameButton.setOnClickListener {
            val intent = Intent(this, ArtRecommendationActivity::class.java)
            startActivity(intent)
        }

        // 마이페이지 이동
        val MypageButton = findViewById<ImageView>(R.id.Mypage_Button)
        MypageButton.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onItemClick(position: Int) {
        val clickedArtwork = artworks[position]

        // ArtworkDetailActivity로 이동하는 인텐트 생성
        val intent = Intent(this, ReviewActivity::class.java).apply {
            putExtra("title", clickedArtwork.title)
            putExtra("author", clickedArtwork.author)
        }
        startActivity(intent)  // 인텐트를 사용해 새로운 액티비티 시작
    }

}