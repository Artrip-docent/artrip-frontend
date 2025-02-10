//package com.example.docent
//
//import android.Manifest
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.Bundle
//import android.speech.RecognitionListener
//import android.speech.RecognizerIntent
//import android.speech.SpeechRecognizer
//import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import com.example.docent.databinding.ActivitySpeakBinding
//
//class SpeakActivity : AppCompatActivity() {
//
//    // Data Binding 클래스에서 자동 생성된 Binding 객체
//    private lateinit var binding: ActivitySpeakBinding
//
//    // 음성 인식을 위한 SpeechRecognizer
//    private lateinit var speechRecognizer: SpeechRecognizer
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // 1. Data Binding 객체 생성
//        binding = ActivitySpeakBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        // 엣지 투 엣지(Edge-to-Edge) 설정
//        enableEdgeToEdge()
//
//        // 시스템 바(상단/하단 바) 패딩 적용
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        // 2. 오디오 권한 요청 함수
//        requestAudioPermission()
//
//        // 3. RecognizerIntent 설정
//        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
//            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
//            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")  // 한국어 인식
//        }
//
//        // 4. 음성 인식 시작 버튼 클릭 이벤트
//        binding.btnSpeech.setOnClickListener {
//            // SpeechRecognizer 생성 후 시작
//            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
//                setRecognitionListener(recognitionListener)
//                startListening(intent)
//            }
//        }
//    }
//
//    // 권한 요청 함수
//    private fun requestAudioPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
//            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
//            != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.RECORD_AUDIO),
//                0
//            )
//        }
//    }
//
//    // 음성인식 상태 변화를 받는 Listener 구현
//    private val recognitionListener: RecognitionListener = object : RecognitionListener {
//        override fun onReadyForSpeech(params: Bundle) {
//            Toast.makeText(applicationContext, "음성인식 시작", Toast.LENGTH_SHORT).show()
//            binding.tvState.text = "이제 말씀하세요!"
//        }
//
//        override fun onBeginningOfSpeech() {
//            binding.tvState.text = "잘 듣고 있어요."
//        }
//
//        override fun onRmsChanged(rmsdB: Float) {}
//        override fun onBufferReceived(buffer: ByteArray) {}
//
//        override fun onEndOfSpeech() {
//            binding.tvState.text = "끝!"
//        }
//
//        override fun onError(error: Int) {
//            val message = when (error) {
//                SpeechRecognizer.ERROR_AUDIO -> "오디오 에러"
//                SpeechRecognizer.ERROR_CLIENT -> "클라이언트 에러"
//                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "퍼미션 없음"
//                SpeechRecognizer.ERROR_NETWORK -> "네트워크 에러"
//                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트워크 타임아웃"
//                SpeechRecognizer.ERROR_NO_MATCH -> "찾을 수 없음"
//                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RECOGNIZER 가 바쁨"
//                SpeechRecognizer.ERROR_SERVER -> "서버 에러"
//                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "말하는 시간 초과"
//                else -> "알 수 없는 오류 발생"
//            }
//            binding.tvState.text = "에러 발생: $message"
//        }
//
//        override fun onResults(results: Bundle) {
//            val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
//            matches?.let {
//                val speechResult = it[0] // 가장 정확한 인식 결과 선택
//                val intent = Intent().apply {
//                    putExtra("speech_result", speechResult)
//                }
//                setResult(RESULT_OK, intent)
//                finish() // 현재 액티비티 종료 후 ChatActivity로 돌아감
//            }
//        }
//
//        override fun onPartialResults(partialResults: Bundle) {}
//        override fun onEvent(eventType: Int, params: Bundle) {}
//
//
//
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        // SpeechRecognizer가 초기화되었을 때만 destroy 호출
//        if (::speechRecognizer.isInitialized) {
//            speechRecognizer.destroy()
//        }
//    }
//}
package com.example.docent

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class SpeakActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_SPEECH_INPUT = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // UI가 필요하지 않다면 바로 음성 인식 실행
        startVoiceRecognition()
    }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            // 자유 형식(free-form) 음성 인식
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            // 사용자의 기본 언어 사용
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            // 사용자에게 안내 메시지 표시
            putExtra(RecognizerIntent.EXTRA_PROMPT, "말씀해주세요.")
        }
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "음성 인식 기능을 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == Activity.RESULT_OK && data != null) {
            // 음성 인식 결과는 String ArrayList로 반환됨 (최우선 결과가 첫 번째 항목)
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val recognizedText = result?.get(0) ?: ""

            // 결과를 ChatActivity로 반환
            val returnIntent = Intent().apply {
                putExtra("speech_result", recognizedText)
            }
            setResult(Activity.RESULT_OK, returnIntent)
        }
        finish() // 액티비티 종료
    }
}