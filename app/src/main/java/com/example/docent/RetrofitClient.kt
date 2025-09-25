package com.example.docent

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://192.168.78.53:8000/" // Django 서버 주소
    //    private const val BASE_URL = "https://b869-218-154-254-94.ngrok-free.app/"
    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            // 타임아웃 상향
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .callTimeout(0, TimeUnit.MILLISECONDS) // 전체 호출에는 제한 두지 않음
            .build()
    }
    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://192.168.78.53:8000/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    data class ArtworkResponse(
        val artwork_id: Int,
        val artwork_name: String,
        val artist: String,
        val year: String,
        val description: String
    )
}
