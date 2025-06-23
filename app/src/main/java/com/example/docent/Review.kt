package com.example.docent

// 데이터 클래스
data class Review(
    val id: Int,
    val exhibition: Int,
    val author: Int,
    val author_name: String,
    val content: String,
    val rating: Float,
    val created_at: String
)