package com.example.docent

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// 로그인 요청/응답 모델
data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val access: String,
    val refresh: String,
    val user_id: Int
)

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Edge-to-Edge 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val emailEditText = findViewById<EditText>(R.id.emailInput)
        val passwordEditText = findViewById<EditText>(R.id.passwordInput)

        // 로그인 버튼 클릭 시 취향분석 화면으로 전환
        val loginButton: Button = findViewById(R.id.login)
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = LoginRequest(email = email, password = password)
            RetrofitClient.instance.loginUser(request).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        if (loginResponse != null) {
                            // ✅ 토큰 저장
                            val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                            prefs.edit().apply {
                                putString("accessToken", loginResponse.access)
                                putString("refreshToken", loginResponse.refresh)
                                putInt("user_id", loginResponse.user_id)
                                apply()
                            }

                            Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@LoginActivity, Keyword1Activity::class.java)
                            startActivity(intent)
                            finish()
                        }else {
                            Toast.makeText(this@LoginActivity, "응답이 비어 있습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "로그인 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "에러: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
}}


