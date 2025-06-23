package com.example.docent

data class ReviewRequest(
    val exhibition: Int,
    val content: String,
    val rating: Float
)
