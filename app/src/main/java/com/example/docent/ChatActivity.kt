package com.example.docent

import android.content.Intent
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
    private var isTTSInitialized = false
    private var ttsBuffer: String = ""
    private lateinit var speechText: EditText
    private lateinit var sendTextBtn: Button
    private lateinit var speechBtn: ImageButton

    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    private var sseThread: Thread? = null  // SSE 쓰레드 관리

    private var firstSpoken = false          // 첫 문장 발화 여부
    private var spokenSentences = mutableSetOf<String>()  // 중복 방지용



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        textToSpeech = TextToSpeech(this, this)

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        speechText = findViewById(R.id.speech_text)
        sendTextBtn = findViewById(R.id.send_text_btn)
        speechBtn = findViewById(R.id.speech_btn)

        chatAdapter = ChatAdapter(messages)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = chatAdapter

        val description = intent.getStringExtra("description") ?: "설명을 불러올 수 없습니다."
        val title = intent.getStringExtra("title") ?: "제목 없음"
        val artist = intent.getStringExtra("artist") ?: "작가 정보 없음"
        val year = intent.getStringExtra("year") ?: "연도 정보 없음"
//        val artworkId = intent.getIntExtra("artwork_id", -1)
//        val exhibitionId = intent.getIntExtra("exhibition_id", -1)
        Log.d("ChatActivity", "📦 인텐트로 전달된 데이터 - 제목=$title, 작가=$artist, 연도=$year, 설명=$description")

        if (messages.isEmpty()) {
            val initialMessage = """
            |제목: $title
            |작가: $artist
            |연도: $year
                
      
            |$description
            """.trimMargin()

            runOnUiThread {
                messages.add(ChatMessage(initialMessage, false)) // 챗봇 메시지로 추가
                chatAdapter.notifyItemInserted(messages.size - 1) // UI 업데이트
                chatRecyclerView.scrollToPosition(messages.size - 1) // 화면 아래로 스크롤
            }

        }

        speechBtn.setOnClickListener {
            val intent = Intent(this, SpeakActivity::class.java)
            speechActivityResultLauncher.launch(intent)
        }

        sendTextBtn.setOnClickListener {
            val userMessage = speechText.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                sendMessageToServer(userMessage)
                speechText.text.clear()
            }
        }
    }

    // SSE 응답을 받아 처리하는 함수
    private fun listenToSSE(userMessage: String, aiMessageIndex: Int) {
        // 이전 SSE 쓰레드 종료
        sseThread?.interrupt()
        val artworkId = intent.getIntExtra("artwork_id", -1)
        val exhibitionId = intent.getIntExtra("exhibition_id", -1)
        val token = "Bearer ${getSharedPreferences("auth", MODE_PRIVATE).getString("accessToken", null)}"
        val userId = getSharedPreferences("auth", MODE_PRIVATE).getInt("user_id", -1)
        val jsonObject = JsonObject().apply {
            addProperty("message", userMessage)
            addProperty("artwork_id", artworkId)                    // 추가된 artwork ID
            addProperty("exhibition_id", exhibitionId)
            addProperty("user_id", userId)
        }

        val call = RetrofitClient.instance.sendChatMessage(token, jsonObject)


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

                                updateChatAndTTS(chunkMessage, aiMessageIndex)
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

    // AI 응답을 추가하고 TTS로 읽는 함수
    private fun updateChatAndTTS(chunkMessage: String, aiMessageIndex: Int) {
        runOnUiThread {
            messages[aiMessageIndex] = ChatMessage(messages[aiMessageIndex].text + chunkMessage, false)
            chatAdapter.notifyItemChanged(aiMessageIndex)
            chatRecyclerView.scrollToPosition(messages.size - 1)

            // ✅ buffer 누적
            ttsBuffer += chunkMessage

            // ✅ 문장 단위로 분해
            val sentenceRegex = Regex("([^.?!]+[.?!])")
            val matches = sentenceRegex.findAll(ttsBuffer).toList()

            if (matches.isNotEmpty()) {
                val lastMatch = matches.last()
                val endIndex = lastMatch.range.last + 1

                for ((_, match) in matches.withIndex()) {
                    val sentence = match.value.trim()
                    if (sentence.isNotEmpty() && spokenSentences.add(sentence)) { // ✅ 중복 방지
                        speakText(sentence)
                    }
                }

                // spoken되지 않은 부분만 buffer에 남김
                ttsBuffer = if (endIndex < ttsBuffer.length) {
                    ttsBuffer.substring(endIndex)
                } else {
                    ""
                }
            }
        }
    }


    private fun sendMessageToServer(message: String,) {
        ttsBuffer = ""
        spokenSentences.clear()  // ✅ 매 질문마다 초기화

        messages.add(ChatMessage(message, true))
        chatAdapter.notifyItemInserted(messages.size - 1)
        chatRecyclerView.scrollToPosition(messages.size - 1)

        messages.add(ChatMessage("", false))
        val aiMessageIndex = messages.size - 1
        chatAdapter.notifyItemInserted(aiMessageIndex)
        val artworkId = intent.getIntExtra("artwork_id", -1)
        val exhibitionId = intent.getIntExtra("exhibition_id", -1)
        val token = "Bearer ${getSharedPreferences("auth", MODE_PRIVATE).getString("accessToken", null)}"
        val userId = getSharedPreferences("auth", MODE_PRIVATE).getInt("user_id", -1)
        val jsonObject = JsonObject().apply {
            addProperty("message", message)
            addProperty("artwork_id", artworkId)                    // 추가된 artwork ID
            addProperty("exhibition_id", exhibitionId)
            addProperty("user_id", userId)
        }
        val call = RetrofitClient.instance.sendChatMessage(token, jsonObject)

        listenToSSE(message, aiMessageIndex)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val languageStatus = textToSpeech.setLanguage(Locale.KOREAN)
            if (languageStatus == TextToSpeech.LANG_MISSING_DATA ||
                languageStatus == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                Toast.makeText(this, "언어를 지원할 수 없습니다.", Toast.LENGTH_SHORT).show()
            } else {
                isTTSInitialized = true
                textToSpeech.setSpeechRate(1.0f)
            }
        } else {
            Toast.makeText(this, "음성전환 엔진 에러입니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun speakText(text: String) {
        if (isTTSInitialized) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, null)
        } else {
            Log.e("ChatActivity", "TTS가 초기화되지 않았습니다!")
        }
    }

    private val speechActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val speechResult = result.data?.getStringExtra("speech_result")
                speechResult?.let {
                    sendMessageToServer(it)
                }
            }
        }

    override fun onDestroy() {
        if (this::textToSpeech.isInitialized) {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
        super.onDestroy()
    }
}