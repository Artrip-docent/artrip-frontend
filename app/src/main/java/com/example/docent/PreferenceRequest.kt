package com.example.docent

data class PreferenceRequest (
    val user_id: String,
    val artwork_ids: List<Int>
)