package com.example.docent

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8000/" // Django 서버 주소
    //    private const val BASE_URL = "https://b869-218-154-254-94.ngrok-free.app/"
    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }
    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://172.30.1.35:8000/")
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
