package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MypageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)


        val ticketImage = findViewById<ImageView>(R.id.ticket)
        ticketImage.setOnClickListener {
            val intent = Intent(this, exhibitionviewingActivity::class.java)
            startActivity(intent)
            finish()
        }

        val heartImage = findViewById<ImageView>(R.id.heart)
        heartImage.setOnClickListener {
            val intent = Intent(this, GoodexhibitionActivity::class.java)
            startActivity(intent)
            finish()
        }

        val logoutText: TextView = findViewById(R.id.logout)
        logoutText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val museumImage = findViewById<ImageView>(R.id.museum)
        museumImage.setOnClickListener {
            val intent = Intent(this, ArtRecommendationActivity::class.java)
            startActivity(intent)
            finish()
        }

        val chatImage = findViewById<ImageView>(R.id.chat)
        chatImage.setOnClickListener {
            val intent = Intent(this, ChathistoryActivity::class.java)
            startActivity(intent)
            finish()
        }
        val cameraButton = findViewById<ImageView>(R.id.Camera_Button)
        cameraButton.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        // 커뮤니티 화면으로 이동
        val comuButton = findViewById<ImageView>(R.id.Commu_Button)
        comuButton.setOnClickListener {
            val intent = Intent(this, communityActivity::class.java)
            startActivity(intent)
        }

        // 커뮤니티 화면으로 이동
        val settingButton = findViewById<ImageView>(R.id.setting)
        settingButton.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }

    }

}