package com.example.docent

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler
import android.os.Looper

class SplashActivity : AppCompatActivity() {

    // 스플래시 대기시간 (밀리초) = 2000ms = 2초
    private val SPLASH_DELAY: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 핸들러로 딜레이 처리
        Handler(Looper.getMainLooper()).postDelayed({
            // 메인 액티비티로 이동
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            // 스플래시 액티비티 종료
            finish()
            // (옵션) 화면 전환 애니메이션 추가
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, SPLASH_DELAY)
    }
}