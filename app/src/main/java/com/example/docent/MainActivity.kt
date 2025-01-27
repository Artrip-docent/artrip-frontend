package com.example.docent

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 회원가입 버튼 클릭 시 RegisterActivity로 이동
        val signUpButton: Button = findViewById(R.id.button4)
        signUpButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // 로그인 버튼 클릭 시 로그인 화면으로 전환
        val loginButton: Button = findViewById(R.id.button3)
        loginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }



    }
}