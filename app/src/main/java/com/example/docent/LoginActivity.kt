package com.example.docent

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
    val user_id: Int,
    val is_first_login: Boolean // 추가
)

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailEditText = findViewById<EditText>(R.id.emailInput)
        val passwordEditText = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.login)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // 유효성 검사: 이메일 또는 비밀번호 비어있을 때 경고
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val loginRequest = LoginRequest(email, password)
            RetrofitClient.instance.loginUser(loginRequest)
                .enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(
                        call: Call<LoginResponse>,
                        response: Response<LoginResponse>
                    ) {
                        if (response.isSuccessful) {
                            val loginResponse = response.body()
                            if (loginResponse != null) {
                                // SharedPreferences 저장
                                val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                                prefs.edit().apply {
                                    putString("accessToken", loginResponse.access)
                                    putString("refreshToken", loginResponse.refresh)
                                    putInt("user_id", loginResponse.user_id)
                                    putBoolean("isFirstLogin", loginResponse.is_first_login)
                                    apply()
                                }

                                Log.d("LoginDebug", "is_first_login: ${loginResponse.is_first_login}")
                                Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()

                                // 분기 처리: 첫 로그인 여부에 따라 이동
                                val intent = if (loginResponse.is_first_login) {
                                    Intent(this@LoginActivity, PreferenceanalysisActivity::class.java)
                                } else {
                                    Intent(this@LoginActivity, ArtRecommendationActivity::class.java)
                                }
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "로그인 실패", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, "네트워크 오류", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }
}
