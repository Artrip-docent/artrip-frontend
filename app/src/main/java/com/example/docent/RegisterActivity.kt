package com.example.docent

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val arrowImage = findViewById<ImageView>(R.id.arrow1)
        arrowImage.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // 다음 버튼 클릭 시 등록2 화면으로 전환
        val nextButton: Button = findViewById(R.id.next1)
        nextButton.setOnClickListener {
            val intent = Intent(this, Register2Activity::class.java)
            startActivity(intent)
        }

    }
}