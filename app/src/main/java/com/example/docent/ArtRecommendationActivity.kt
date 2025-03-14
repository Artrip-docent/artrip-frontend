package com.example.docent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class ArtRecommendationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_art_recommendation)
        // Camera Button 클릭 이벤트 설정
        val cameraButton = findViewById<ImageView>(R.id.Camera_Button)
        cameraButton.setOnClickListener {
            Log.d("ArtRecommendation", "카메라 버튼 클릭됨") // 디버그 로그
            startActivity(Intent(this, CameraActivity::class.java))
        }

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
}