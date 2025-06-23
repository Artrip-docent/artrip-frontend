package com.example.docent

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ChatHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChatHistoryAdapter
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat) // 기존 채팅 화면 재사용
        // 입력창 제거
        findViewById<LinearLayout>(R.id.main).findViewById<LinearLayout>(R.id.main)
            .findViewById<View>(R.id.send_text_btn).visibility = View.GONE
        findViewById<View>(R.id.speech_text).visibility = View.GONE
        findViewById<View>(R.id.speech_btn).visibility = View.GONE
        val titleView = findViewById<TextView>(R.id.chatTitle)
        titleView.visibility = View.VISIBLE
        recyclerView = findViewById(R.id.chatRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ChatHistoryAdapter(messages)
        recyclerView.adapter = adapter

        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("accessToken", null)
        val userId = prefs.getInt("user_id", -1)
        val artworkId = intent.getIntExtra("artwork_id", -1)
        val exhibitionId = intent.getIntExtra("exhibition_id", -1)

        if (token.isNullOrBlank() || userId == -1 || artworkId == -1 || exhibitionId == -1) {
            Toast.makeText(this, "잘못된 접근입니다", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchChatHistory("Bearer $token", userId, artworkId, exhibitionId)
    }

    private fun fetchChatHistory(token: String, userId: Int, artworkId: Int, exhibitionId: Int) {
        RetrofitClient.instance.getChatHistory(token, userId, artworkId, exhibitionId)
            .enqueue(object : Callback<List<ChatHistoryItem>> {
                override fun onResponse(
                    call: Call<List<ChatHistoryItem>>,
                    response: Response<List<ChatHistoryItem>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val history = response.body()!!
                        messages.clear()
                        for (item in history) {
                            messages.add(ChatMessage(item.question, true))  // 사용자
                            messages.add(ChatMessage(item.answer, false))   // GPT
                        }
                        adapter.notifyDataSetChanged()
                        recyclerView.scrollToPosition(messages.size - 1)
                    } else {
                        Toast.makeText(this@ChatHistoryActivity, "채팅 기록이 없습니다", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<ChatHistoryItem>>, t: Throwable) {
                    Toast.makeText(this@ChatHistoryActivity, "서버 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}