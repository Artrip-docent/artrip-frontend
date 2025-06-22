package com.example.docent

import android.content.Intent
import android.os.Bundle
import android.util.Log
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

        userId = getSharedPreferences("auth", MODE_PRIVATE).getInt("user_id", -1)
        if (userId == -1) {
            Log.e("User", "유저 ID가 유효하지 않음")
            return
        }

        fetchSortedExhibitions()

        searchButton.setOnClickListener {
            val query = searchInput.text.toString().trim()
            if (query.isNotEmpty()) {
                searchExhibitions(query)
            }
        }

        startViewButton.setOnClickListener {
            selectedExhibitionId?.let {
                val intent = Intent(this, CameraActivity::class.java)
                intent.putExtra("EXHIBITION_ID", it)
                startActivity(intent)
            }
        }
        cancelButton = findViewById(R.id.cancel_button)  // XML에서 id 맞춰줄 것

        cancelButton.setOnClickListener {
            searchInput.setText("")               // 검색창 텍스트 지우기
            fetchSortedExhibitions()             // 전체 전시 목록 다시 불러오기
        }


    }

    private fun fetchSortedExhibitions() {
        RetrofitClient.instance.getSortedExhibitions(userId).enqueue(object : Callback<List<Exhibition>> {
            override fun onResponse(call: Call<List<Exhibition>>, response: Response<List<Exhibition>>) {
                if (response.isSuccessful) {
                    val exhibitions = response.body() ?: emptyList()
                    exhibitionAdapter = ExhibitionSelectAdapter(exhibitions) { selectedId ->
                        selectedExhibitionId = selectedId
                        startViewButton.isEnabled = true
                    }
                    recyclerView.adapter = exhibitionAdapter
                }
            }

            override fun onFailure(call: Call<List<Exhibition>>, t: Throwable) {
                Log.e("ExhibitionSelect", "전시 리스트 로드 실패: ${t.message}")
            }
        })
    }

    private fun searchExhibitions(query: String) {
        RetrofitClient.instance.searchExhibitions(query).enqueue(object : Callback<ExhibitionSearchResponse> {
            override fun onResponse(call: Call<ExhibitionSearchResponse>, response: Response<ExhibitionSearchResponse>) {
                if (response.isSuccessful) {
                    val exhibitions = response.body()?.results?.map {
                        Exhibition(
                            id = it.id,
                            title = it.title,
                            period = "${it.start_date} ~ ${it.end_date}",
                            location = it.location,
                            imageUrl = it.image_url
                        )
                    } ?: emptyList()
                    exhibitionAdapter = ExhibitionSelectAdapter(exhibitions) { selectedId ->
                        selectedExhibitionId = selectedId
                        startViewButton.isEnabled = true
                    }
                    recyclerView.adapter = exhibitionAdapter
                }
            }

            override fun onFailure(call: Call<ExhibitionSearchResponse>, t: Throwable) {
                Log.e("ExhibitionSelect", "검색 실패: ${t.message}")
            }
        })
    }
}
