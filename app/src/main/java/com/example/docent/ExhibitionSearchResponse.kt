// 파일 이름: ExhibitionSearchResponse.kt

package com.example.docent

data class ExhibitionSearchResponse(
    val results: List<SearchExhibition>
)

data class SearchExhibition(
    val id: Int,
    val title: String,
    val start_date: String,
    val end_date: String,
    val location: String,
    val image_url: String
)
