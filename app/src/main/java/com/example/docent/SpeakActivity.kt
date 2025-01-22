package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.databinding.ActivitySpeakBinding

class SpeakActivity : AppCompatActivity() {

    // Data Binding 클래스에서 자동 생성된 Binding 객체
    private lateinit var binding: ActivitySpeakBinding

    // 음성 인식을 위한 SpeechRecognizer
    private lateinit var speechRecognizer: SpeechRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Data Binding 객체 생성
        binding = ActivitySpeakBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 엣지 투 엣지(Edge-to-Edge) 설정
        enableEdgeToEdge()

        // 시스템 바(상단/하단 바) 패딩 적용
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 2. 오디오 권한 요청 함수
        requestAudioPermission()

        // 3. RecognizerIntent 설정
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")  // 한국어 인식
        }

        // 4. 음성 인식 시작 버튼 클릭 이벤트
        binding.btnSpeech.setOnClickListener {
            // SpeechRecognizer 생성 후 시작
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(recognitionListener)
                startListening(intent)
            }
        }
    }

    // 권한 요청 함수
    private fun requestAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                0
            )
        }
    }

    // 음성인식 상태 변화를 받는 Listener 구현
    private val recognitionListener: RecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle) {
            Toast.makeText(applicationContext, "음성인식 시작", Toast.LENGTH_SHORT).show()
            binding.tvState.text = "이제 말씀하세요!"
        }

        override fun onBeginningOfSpeech() {
            binding.tvState.text = "잘 듣고 있어요."
        }

        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray) {}

        override fun onEndOfSpeech() {
            binding.tvState.text = "끝!"
        }

        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "오디오 에러"
                SpeechRecognizer.ERROR_CLIENT -> "클라이언트 에러"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "퍼미션 없음"
                SpeechRecognizer.ERROR_NETWORK -> "네트워크 에러"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트워크 타임아웃"
                SpeechRecognizer.ERROR_NO_MATCH -> "찾을 수 없음"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RECOGNIZER 가 바쁨"
                SpeechRecognizer.ERROR_SERVER -> "서버 에러"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "말하는 시간 초과"
                else -> "알 수 없는 오류 발생"
            }
            binding.tvState.text = "에러 발생: $message"
        }

        override fun onResults(results: Bundle) {
            // 인식된 문자열들의 리스트
            val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            matches?.let {
                // 여러 결과가 나올 수 있으므로 줄바꿈(\n)으로 연결
                binding.textView.text = it.joinToString("\n")
            }
        }

        override fun onPartialResults(partialResults: Bundle) {}
        override fun onEvent(eventType: Int, params: Bundle) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        // SpeechRecognizer가 초기화되었을 때만 destroy 호출
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }
    }
}