package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LifecycleOwner
import java.nio.ByteBuffer

class CameraActivity : AppCompatActivity() {

    private var imageCapture: ImageCapture? = null  // ImageCapture 객체 초기화
    private val CAMERA_PERMISSION_REQUEST_CODE = 1001  // 카메라 권한 요청 코드
    private var capturedImage: Bitmap? = null  // 캡처된 이미지를 저장할 Bitmap 변수
    private lateinit var capturedImageView: ImageView  // 캡처된 이미지를 표시할 ImageView
    private lateinit var previewView: PreviewView  // 카메라 미리보기 PreviewView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_camera)

        // 화면 인셋 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // View 초기화
        previewView = findViewById(R.id.previewView)
        capturedImageView = findViewById(R.id.captured_image_view)
        val comuButton = findViewById<ImageButton>(R.id.Commu_Button)
        val frameButton = findViewById<ImageButton>(R.id.Frame_Button)
        val MypageButton = findViewById<ImageButton>(R.id.Mypage_Button)
        val listen = findViewById<Button>(R.id.speak_btn)

        // 커뮤니티 화면으로 이동
        comuButton.setOnClickListener {
            val intent = Intent(this, communityActivity::class.java)
            startActivity(intent)
        }

        // 작품추천화면으로 이동
        frameButton.setOnClickListener {
            val intent = Intent(this, ArtRecommendationActivity::class.java)
            startActivity(intent)
        }

        // 마이페이지 이동
        MypageButton.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }


        // 카메라 권한 요청
        requestCameraPermission()

        // 사진 촬영 버튼 클릭 리스너 설정
        findViewById<Button>(R.id.capture_button).setOnClickListener {
            takePhoto()
        }
        listen.setOnClickListener{
            var intent = Intent(this, ChatActivity::class.java)
            startActivity(intent)
        }
    }

    // 카메라 권한 요청 및 처리 함수
    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            startCamera()
        }
    }

    // 권한 요청 결과 처리 함수
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            Toast.makeText(this, "카메라 권한이 필요합니다", Toast.LENGTH_SHORT).show()
        }
    }

    // 카메라 시작 함수
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // 미리보기 설정
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

    // 사진 촬영 함수
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                // ByteBuffer를 Bitmap으로 변환하여 ImageView에 설정
                val buffer: ByteBuffer = imageProxy.planes[0].buffer
                val bytes = ByteArray(buffer.capacity())
                buffer.get(bytes)
                capturedImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                // Matrix 생성 및 회전 설정
                val matrix = Matrix().apply {
                    postRotate(90f)  // 90도 회전
                }

                // 회전된 비트맵 생성
                val rotatedBitmap = Bitmap.createBitmap(
                    capturedImage!!,
                    0, 0,
                    capturedImage!!.width, capturedImage!!.height,
                    matrix,
                    true
                )

                // 회전된 이미지를 ImageView에 설정
                capturedImageView.setImageBitmap(rotatedBitmap)

                imageProxy.close()
                //Toast.makeText(applicationContext, "사진이 저장되었습니다.", Toast.LENGTH_SHORT).show()
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("MainActivity", "사진 캡처 실패: ${exception.message}", exception)
            }
        })
    }
}