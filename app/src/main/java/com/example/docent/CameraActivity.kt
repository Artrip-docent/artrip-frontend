package com.example.docent

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LifecycleOwner
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : AppCompatActivity() {

    private var imageCapture: ImageCapture? = null
    private val CAMERA_PERMISSION_REQUEST_CODE = 1001
    private var capturedImage: Bitmap? = null
    private lateinit var capturedImageView: ImageView
    private lateinit var previewView: PreviewView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_camera)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        previewView = findViewById(R.id.previewView)
        capturedImageView = findViewById(R.id.captured_image_view)
        val comuButton = findViewById<ImageButton>(R.id.Commu_Button)
        val frameButton = findViewById<ImageButton>(R.id.Frame_Button)
        val MypageButton = findViewById<ImageButton>(R.id.Mypage_Button)
        val listen = findViewById<Button>(R.id.speak_btn)

        comuButton.setOnClickListener {
            val intent = Intent(this, communityActivity::class.java)
            startActivity(intent)
        }

        frameButton.setOnClickListener {
            val intent = Intent(this, ArtRecommendationActivity::class.java)
            startActivity(intent)
        }

        MypageButton.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }

        listen.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }

        requestCameraPermission()

        findViewById<Button>(R.id.capture_button).setOnClickListener {
            takePhoto()
        }

        // Mock 데이터 호출 테스트
        fetchMockData()
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            startCamera()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            Toast.makeText(this, "카메라 권한이 필요합니다", Toast.LENGTH_SHORT).show()
        }
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Toast.makeText(this, "카메라를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File.createTempFile(
            "JPEG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}_",
            ".jpg",
            cacheDir
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
                Log.d("CameraActivity", "사진이 저장되었습니다: $savedUri")

                // ChatActivity로 바로 이동
                val intent = Intent(this@CameraActivity, ChatActivity::class.java).apply {
                    putExtra("imageUri", savedUri.toString())
                }
                startActivity(intent)
                finish()
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraActivity", "사진 저장 실패: ${exception.message}", exception)
            }
        })
    }


    private fun uploadImageToServer(file: File) {
        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), file)
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        val apiService = RetrofitClient.instance
        apiService.uploadArtwork(body).enqueue(object : Callback<RetrofitClient.ArtworkResponse> {
            override fun onResponse(
                call: Call<RetrofitClient.ArtworkResponse>,
                response: Response<RetrofitClient.ArtworkResponse>
            ) {
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        Log.d("CameraActivity", "서버 응답 데이터: 제목=${data.title}, 작가=${data.artist}, 연도=${data.year}, 스타일=${data.style}, 설명=${data.description}")
                        // UI 업데이트 (예: 텍스트뷰 업데이트)
                        // exampleTextView.text = "작품 제목: ${data.title}\n작가: ${data.artist}"

                        // ChatActivity로 서버 응답 데이터 전달
                        val intent = Intent(this@CameraActivity, ChatActivity::class.java).apply {
                            putExtra("description", data.description)
                            putExtra("title", data.title)
                            putExtra("artist", data.artist)
                            putExtra("year", data.year.toString())
                            putExtra("style", data.style)
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Log.e("CameraActivity", "서버 응답 데이터가 비어 있습니다.")
                    }
                } else {
                    Log.e("CameraActivity", "응답 오류 코드: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<RetrofitClient.ArtworkResponse>, t: Throwable) {
                Log.e("CameraActivity", "서버 통신 실패: ${t.message}")
            }
        })
    }



    private fun fetchMockData() {
        val apiService = RetrofitClient.instance
        apiService.getMockArtwork().enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("CameraActivity", "Mock Data: $data")
                } else {
                    Log.e("CameraActivity", "Mock Data Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Log.e("CameraActivity", "Mock Data Failure: ${t.message}")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
        }, ContextCompat.getMainExecutor(this))
    }
}
