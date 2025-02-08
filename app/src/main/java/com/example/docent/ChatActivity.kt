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

    private fun listenToSSE(userMessage: String) {
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

                        runOnUiThread {
                            messages.add(ChatMessage("", false)) // 빈 메시지 추가 (Chunk별 업데이트)
                            chatAdapter.notifyItemInserted(messages.size - 1)
                        }

                        val lastMessageIndex = messages.size - 1

                        while (reader.readLine().also { line = it } != null) {
                            if (!line.isNullOrBlank() && line!!.startsWith("data:")) {
                                val chunkMessage = line!!.removePrefix("data:").trim()

                                runOnUiThread {
                                    messages[lastMessageIndex] = ChatMessage(messages[lastMessageIndex].text + chunkMessage, false)
                                    chatAdapter.notifyItemChanged(lastMessageIndex)
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
        messages.add(ChatMessage(message, true)) // 사용자의 메시지 추가
        chatAdapter.notifyItemInserted(messages.size - 1)
        chatRecyclerView.scrollToPosition(messages.size - 1)

        listenToSSE(message)

        val apiService = RetrofitClient.instance
        val jsonObject = JsonObject().apply {
            addProperty("message", message)
        }
        val call = apiService.sendChatMessage(jsonObject)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.let { responseBody ->
                        val botResponse = responseBody.string()
                        runOnUiThread {
                            messages.add(ChatMessage(botResponse, false)) // 챗봇 응답 추가
                            chatAdapter.notifyItemInserted(messages.size - 1)
                            chatRecyclerView.scrollToPosition(messages.size - 1)

                            // 챗봇 응답을 음성으로 출력
                            speakText(botResponse)
                        }
                    }
                } else {
                    Toast.makeText(this@ChatActivity, "서버 오류 발생", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@ChatActivity, "네트워크 오류 발생", Toast.LENGTH_SHORT).show()
            }
        })
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
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
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


