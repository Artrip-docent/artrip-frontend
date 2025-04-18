package com.example.docent

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.*
import com.google.gson.JsonObject

interface ApiService {
    @Multipart

    @POST("artworks/upload/") // Django 서버의 이미지 업로드 엔드포인트
    fun uploadArtwork(
        @Part image: MultipartBody.Part
    ): Call<RetrofitClient.ArtworkResponse>

    @Streaming
    @POST("api/chat/") // SSE 지원하는 단일 API
    fun sendChatMessage(@Body payload: JsonObject): Call<ResponseBody>

    // 사용자 취향 분석 API
    @POST("artworks/analyze-preference/")
    fun analyzePreference(@Body request: PreferenceRequest): Call<PreferenceResponse>

    @GET("artworks/random/")
    fun getRandomArtworks(): Call<List<Artwork>>

    @GET("api/exhibitions/")
    fun getExhibitions(): Call<List<Exhibition>>}


