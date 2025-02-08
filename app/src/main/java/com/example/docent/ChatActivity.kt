package com.example.docent

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedReader
import java.io.InputStreamReader
import android.util.Log
import com.google.gson.JsonObject


class ChatActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var textToSpeech: TextToSpeech


    private lateinit var speechText: EditText
    private lateinit var sendTextBtn: Button
    private lateinit var speechBtn: ImageButton

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    private var sseThread: Thread? = null
    private var lastUserMessage: String = "" // 마지막으로 보낸 메시지 저장


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // TextToSpeech 초기화 추가
        textToSpeech = TextToSpeech(this, this)

        // UI 요소 초기화
        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        speechText = findViewById(R.id.speech_text)
        sendTextBtn = findViewById(R.id.send_text_btn)
        speechBtn = findViewById(R.id.speech_btn)

        // RecyclerView 초기화
        chatAdapter = ChatAdapter(messages)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = chatAdapter


        // 음성 입력 버튼 클릭 이벤트 //음성입력구현해야함
        speechBtn.setOnClickListener {
            val intent = Intent(this, SpeakActivity::class.java)
            speechActivityResultLauncher.launch(intent)
        }

        // 메시지 전송 버튼 클릭 이벤트
        sendTextBtn.setOnClickListener {
            val userMessage = speechText.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                sendMessageToServer(userMessage)
            }
        }
    }

    // SSE 응답을 받아 처리하는 함수
    private fun listenToSSE(userMessage: String, aiMessageIndex: Int) {
        val jsonObject = JsonObject().apply {
            addProperty("message", userMessage)
        }
        val call = RetrofitClient.instance.sendChatMessage(jsonObject)

        sseThread = Thread {
            try {
                val response = call.execute()
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        val reader = BufferedReader(InputStreamReader(responseBody.byteStream()))
                        var line: String?

                        while (reader.readLine().also { line = it } != null) {
                            if (!line.isNullOrBlank() && line!!.startsWith("data:")) {
                                val chunkMessage = line!!.removePrefix("data: ")

                                runOnUiThread {
                                    // 4️⃣ 기존 AI 메시지 업데이트 (새 메시지를 추가하는 것이 아님)
                                    messages[aiMessageIndex] = ChatMessage(messages[aiMessageIndex].text + chunkMessage, false)
                                    chatAdapter.notifyItemChanged(aiMessageIndex)
                                    chatRecyclerView.scrollToPosition(messages.size - 1)
                                }
                            }
                        }
                    }
                } else {
                    Log.e("ChatActivity", "SSE 연결 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("ChatActivity", "SSE 오류 발생: ${e.message}")
            }
        }
        sseThread?.start()
    }



    // 사용자의 질문을 서버로 전송하는 함수
    private fun sendMessageToServer(message: String) {
        // 1️⃣ 사용자의 입력을 메시지 리스트에 추가
        messages.add(ChatMessage(message, true)) // 사용자 메시지 추가
        chatAdapter.notifyItemInserted(messages.size - 1)
        chatRecyclerView.scrollToPosition(messages.size - 1)

        // 2️⃣ AI의 응답을 위한 빈 말풍선 추가 (SSE 업데이트를 위해)
        messages.add(ChatMessage("", false)) // 빈 AI 응답 추가
        val aiMessageIndex = messages.size - 1
        chatAdapter.notifyItemInserted(aiMessageIndex)

        // 3️⃣ SSE 연결 시작
        listenToSSE(message, aiMessageIndex)
    }

    private val activityResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                textToSpeech = TextToSpeech(this, this, "com.google.android.tts")
            } else {
                val installIntent = Intent()
                installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                startActivity(installIntent)
            }
        }

    // TTS 엔진 초기화
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val languageStatus = textToSpeech.setLanguage(Locale.KOREAN)

            if (languageStatus == TextToSpeech.LANG_MISSING_DATA ||
                languageStatus == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                Toast.makeText(this, "언어를 지원할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "음성전환 엔진 에러입니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // TTS를 통해 챗봇 응답을 음성으로 출력하는 함수
    private fun speakText(text: String) {
        if (::textToSpeech.isInitialized) { // TTS가 초기화되었는지 확인
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            Log.e("ChatActivity", "TTS가 초기화되지 않았습니다!")
        }
    }

    private val speechActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val speechResult = result.data?.getStringExtra("speech_result")
                speechResult?.let {
                    speechText.setText(it) // EditText에 표시
                    sendMessageToServer(it) // 받아온 메시지를 바로 서버로 전송
                }
            }
        }

    override fun onDestroy() {
        if (this::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()  //  마지막에 한 번만 호출
    }
}


