package com.example.docent

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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

    private lateinit var imageViewProfile: ImageView
    private lateinit var editNickname: EditText
    private lateinit var btnSave: Button
    private var selectedImageFile: File? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            imageViewProfile.setImageURI(it)
            selectedImageFile = getFileFromUri(this, it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_edit)

        imageViewProfile = findViewById(R.id.imageViewProfile)
        editNickname = findViewById(R.id.editNickname)
        btnSave = findViewById(R.id.btnSave)

        imageViewProfile.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

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

        val nicknamePart = nickname.toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart = selectedImageFile?.let {
            val requestBody = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("profileImage", it.name, requestBody)
        }

        val token = getToken(this)
        if (token.isEmpty()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val authHeader = "Bearer $token"

        RetrofitClient.instance.updateProfile(authHeader, imagePart, nicknamePart)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ProfileEditActivity, "프로필이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                        // 성공 시 마이페이지로 이동
                        val intent = Intent(this@ProfileEditActivity, MypageActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@ProfileEditActivity, "서버 오류 발생: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(this@ProfileEditActivity, "서버 요청 실패", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun getFileFromUri(context: Context, uri: Uri): File {
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor?.moveToFirst()
        val fileName = nameIndex?.let { cursor.getString(it) } ?: "temp_file"
        cursor?.close()

        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, fileName)
        inputStream?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file
    }

    private fun getToken(context: Context): String {
        val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        return prefs.getString("accessToken", "") ?: ""
    }
}
