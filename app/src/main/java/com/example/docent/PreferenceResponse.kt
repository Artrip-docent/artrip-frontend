package com.example.docent

import com.google.gson.annotations.SerializedName

data class PreferenceResponse(
    val top_movement: String?,
    val movements: List<TagCount>?,   // [{name,count}]
    val moods: List<TagCount>?        // [{name,count}]
)

data class TagCount(
    val name: String,
    val count: Int
)