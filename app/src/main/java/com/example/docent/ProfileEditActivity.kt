package com.example.docent

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var ivProfileBg: ImageView
    private lateinit var btnChangePhoto: ImageButton
    private lateinit var editNickname: EditText
    private lateinit var btnSave: MaterialButton
    private var selectedImageFile: File? = null

    // 이미지를 선택하여 ivProfileBg에 표시합니다.
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            ivProfileBg.setImageURI(it)
            selectedImageFile = getFileFromUri(this, it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        // 뷰 바인딩
        ivProfileBg     = findViewById(R.id.iv_profile_bg)
        btnChangePhoto  = findViewById(R.id.btnChangePhoto)
        editNickname    = findViewById(R.id.editNickname)
        btnSave         = findViewById(R.id.btnSave)

        // 뒤로 가기
        findViewById<ImageView>(R.id.iv_back).setOnClickListener {
            finish()
        }

        // 사진 변경 버튼 클릭 → 갤러리에서 이미지 선택
        btnChangePhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // 확인 버튼 클릭 → 서버에 프로필 업데이트
        btnSave.setOnClickListener {
            updateProfile()
        }
    }

    private fun updateProfile() {
        val nickname = editNickname.text.toString().trim()
        if (nickname.isEmpty()) {
            Toast.makeText(this, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // 닉네임 파트
        val nicknamePart = nickname.toRequestBody("text/plain".toMediaTypeOrNull())

        // 이미지 파트 (선택된 경우에만)
        val imagePart = selectedImageFile?.let {
            val body = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("profileImage", it.name, body)
        }

        // 토큰 꺼내기
        val token = getToken(this)
        if (token.isEmpty()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val authHeader = "Bearer $token"

        // Retrofit 호출
        RetrofitClient.instance
            .updateProfile(authHeader, imagePart, nicknamePart)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@ProfileEditActivity,
                            "프로필이 저장되었습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        // 저장 후 마이페이지로 이동
                        startActivity(Intent(this@ProfileEditActivity, MypageActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this@ProfileEditActivity,
                            "서버 오류: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(
                        this@ProfileEditActivity,
                        "요청 실패: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun getFileFromUri(context: Context, uri: Uri): File {
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor?.moveToFirst()
        val fileName = nameIndex?.let { cursor.getString(it) } ?: "temp.jpg"
        cursor?.close()

        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, fileName)
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    private fun getToken(context: Context): String {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return prefs.getString("accessToken", "") ?: ""
    }
}

