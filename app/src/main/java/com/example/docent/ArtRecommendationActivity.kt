package com.example.docent

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.BufferedReader
import java.io.InputStreamReader

class ArtRecommendationActivity : AppCompatActivity() {
    private lateinit var exhibitionAdapter: ExhibitionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_art_recommendation)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        val exhibitions = readExhibitionsFromCSV()
        exhibitionAdapter = ExhibitionAdapter(exhibitions)
        recyclerView.adapter = exhibitionAdapter

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<ImageView>(R.id.mypage).setOnClickListener {
            startActivity(Intent(this, MypageActivity::class.java))
            finish()
        }

        findViewById<ImageView>(R.id.Camera_Button).setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        findViewById<ImageView>(R.id.Commu_Button).setOnClickListener {
            startActivity(Intent(this, communityActivity::class.java))
        }
    }

    private fun readExhibitionsFromCSV(): List<Exhibition> {
        val exhibitionList = mutableListOf<Exhibition>()
        try {
            val inputStream = assets.open("exhibitions.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.readLine() // 첫 줄 (헤더) 건너뛰기

            reader.forEachLine { line ->
                val tokens = line.split(",")
                if (tokens.size >= 5) {
                    val title = tokens[0].trim()
                    val startDate = tokens[1].trim()
                    val endDate = tokens[2].trim()
                    val location = tokens[3].trim()
                    val imageUrl = tokens[4].trim()
                    val dateRange = "$startDate ~ $endDate"
                    exhibitionList.add(Exhibition(title, dateRange, location, imageUrl))
                }
            }

            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return exhibitionList
    }

}
