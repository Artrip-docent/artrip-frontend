package com.example.docent

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.Toast


data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String,
    val nickname: String,
    val gender: String?
)

data class RegisterResponse(
    val message: String,
    val userId: Int
)



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
            finish() // Register2로 돌아감
        }

        val nameEditText = findViewById<EditText>(R.id.nameInput)
        val maleButton = findViewById<Button>(R.id.maleBtn)
        val femaleButton = findViewById<Button>(R.id.femaleBtn)

        var selectedGender = ""

        maleButton.setOnClickListener {
            selectedGender = "male"
        }

        femaleButton.setOnClickListener {
            selectedGender = "female"
        }

        val email = intent.getStringExtra("email") ?: ""
        val password = intent.getStringExtra("password") ?: ""

        // 다음 버튼 클릭 시 로그인으로 전환
        val nextButton: Button = findViewById(R.id.next3)
        nextButton.setOnClickListener {
            val nickname = nameEditText.text.toString().trim()

            if (nickname.isEmpty()) {
                Toast.makeText(this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = RegisterRequest(
                email = email,
                password = password,
                nickname = nickname,
                username = nickname,
                gender = selectedGender
            )
            RetrofitClient.instance.registerUser(request).enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(
                    call: Call<RegisterResponse>,
                    response: Response<RegisterResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@Register3Activity, "회원가입 성공", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@Register3Activity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@Register3Activity, "회원가입 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    Toast.makeText(this@Register3Activity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}





