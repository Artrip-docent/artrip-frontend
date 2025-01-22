package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity


class Keyword1Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keyword1)

        // Setting up a common method to handle button clicks

        // 작품 추천 화면으로 이동
        val next = findViewById<Button>(R.id.next6)
        next.setOnClickListener {
            val intent = Intent(this, ArtRecommendationActivity::class.java)
            startActivity(intent)
        }

        val artwork1Image = findViewById<ImageView>(R.id.artwork1)
        artwork1Image.setOnClickListener {
            val intent = Intent(this, ArtRecommendationActivity::class.java)
            startActivity(intent)
            finish()
        }

        val artwork2Image = findViewById<ImageView>(R.id.artwork2)
        artwork2Image.setOnClickListener {
            val intent = Intent(this, ArtRecommendationActivity::class.java)
            startActivity(intent)
            finish()
        }

        val artwork3Image = findViewById<ImageView>(R.id.artwork3)
        artwork3Image.setOnClickListener {
            val intent = Intent(this, ArtRecommendationActivity::class.java)
            startActivity(intent)
            finish()
        }

        val artwork4Image = findViewById<ImageView>(R.id.artwork4)
        artwork4Image.setOnClickListener {
            val intent = Intent(this, ArtRecommendationActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    }
