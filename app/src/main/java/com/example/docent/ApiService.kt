package com.example.docent

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.*
import com.google.gson.JsonObject

interface ApiService {
    @Multipart
    @POST("artworks/upload-artwork/") // Django 서버의 이미지 업로드 엔드포인트
    fun uploadArtwork(
        @Part image: MultipartBody.Part
    ): Call<RetrofitClient.ArtworkResponse> // Mock 데이터 (artwork_name과 artist)를 반환

    @POST("chatbot/send-message/") // Django 서버의 엔드포인트 (경로 확인 필수)
    fun sendTextMessage(@Body message: String): Call<ChatResponse>


   @GET("artworks/analyze-image/") // Django 서버의 Mock 데이터 반환 엔드포인트
   fun getMockArtwork(): Call<Map<String, String>> // artwork_name과 artist를 반환
}