package com.example.docent

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExhibitionSelectActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchInput: EditText
    private lateinit var searchButton: TextView
    private lateinit var startViewButton: Button
    private lateinit var exhibitionAdapter: ExhibitionSelectAdapter
    private lateinit var cancelButton: TextView

    private var selectedExhibitionId: Int? = null
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exhibition_select)

        recyclerView = findViewById(R.id.recyclerView)
        searchInput = findViewById(R.id.search_input)
        searchButton = findViewById(R.id.search_button)
        startViewButton = findViewById(R.id.btn_start_view)
        cancelButton = findViewById(R.id.cancel_button)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // 처음에는 비활성화(선택되면 활성화)
        startViewButton.isEnabled = false

        userId = getSharedPreferences("auth", MODE_PRIVATE).getInt("user_id", -1)
        if (userId == -1) {
            Log.e("User", "유저 ID가 유효하지 않음")
            return
        }

        // 최초: 좋아요 우선 + 진행중→예정→지난
        fetchSortedExhibitions()

        // 검색 버튼
        searchButton.setOnClickListener {
            val query = searchInput.text.toString().trim()
            if (query.isNotEmpty()) searchExhibitions(query)
        }

        // 키보드 검색 액션(엔터)
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = searchInput.text.toString().trim()
                if (query.isNotEmpty()) searchExhibitions(query)
                true
            } else false
        }

        // 취소: 검색어 비우고 전체 목록
        cancelButton.setOnClickListener {
            searchInput.setText("")
            selectedExhibitionId = null
            startViewButton.isEnabled = false
            fetchSortedExhibitions()
        }

        // 관람 시작
        startViewButton.setOnClickListener {
            selectedExhibitionId?.let { id ->
                val intent = Intent(this, CameraActivity::class.java).apply {
                    putExtra("EXHIBITION_ID", id)
                }
                startActivity(intent)
            }
        }
    }

    private fun fetchSortedExhibitions() {
        RetrofitClient.instance.getSortedExhibitions(userId)
            .enqueue(object : Callback<List<Exhibition>> {
                override fun onResponse(
                    call: Call<List<Exhibition>>,
                    response: Response<List<Exhibition>>
                ) {
                    if (response.isSuccessful) {
                        val exhibitions = response.body().orEmpty()
                        exhibitionAdapter = ExhibitionSelectAdapter(exhibitions) { selectedId ->
                            selectedExhibitionId = selectedId
                            startViewButton.isEnabled = true
                        }
                        recyclerView.adapter = exhibitionAdapter
                    } else {
                        Log.e("ExhibitionSelect", "목록 실패: code=${response.code()} body=${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<List<Exhibition>>, t: Throwable) {
                    Log.e("ExhibitionSelect", "전시 리스트 로드 실패: ${t.message}", t)
                }
            })
    }

    // 검색 API: 배열(JSON array)로 응답 → List<Exhibition>로 바로 받기
    private fun searchExhibitions(query: String) {
        RetrofitClient.instance.searchExhibitions(query)
            .enqueue(object : Callback<List<Exhibition>> {
                override fun onResponse(
                    call: Call<List<Exhibition>>,
                    response: Response<List<Exhibition>>
                ) {
                    if (response.isSuccessful) {
                        // 새 검색 결과에서는 아직 아무 것도 선택되지 않으므로 비활성화
                        selectedExhibitionId = null
                        startViewButton.isEnabled = false

                        val exhibitions = response.body().orEmpty()
                        exhibitionAdapter = ExhibitionSelectAdapter(exhibitions) { selectedId ->
                            selectedExhibitionId = selectedId
                            startViewButton.isEnabled = true
                        }
                        recyclerView.adapter = exhibitionAdapter
                    } else {
                        Log.e("ExhibitionSelect", "검색 실패: code=${response.code()} body=${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<List<Exhibition>>, t: Throwable) {
                    Log.e("ExhibitionSelect", "검색 실패: ${t.message}", t)
                }
            })
    }
}
