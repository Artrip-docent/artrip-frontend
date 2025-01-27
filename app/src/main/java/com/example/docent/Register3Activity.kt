package com.example.docent

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class Register3Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register3)

        // Edge-to-Edge 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 화살표 이미지 클릭 시 Register2Activity로 전환
        val arrowImage = findViewById<ImageView>(R.id.arrow3)
        arrowImage.setOnClickListener {
            val intent = Intent(this, Register2Activity::class.java)
            startActivity(intent)

            val nextButton: Button = findViewById(R.id.next3)
            nextButton.setOnClickListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }


        }
        // 다음 버튼 클릭 시 로그인으로 전환
        val nextButton: Button = findViewById(R.id.next3)
        nextButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }


    }
}





