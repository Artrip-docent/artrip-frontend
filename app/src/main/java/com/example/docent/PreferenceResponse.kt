package com.example.docent

import com.google.gson.annotations.SerializedName

data class PreferenceResponse (
    @SerializedName("top_tags")
    val topTags: List<String>
)