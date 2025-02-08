package com.example.docent

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://172.30.1.26:8000/" // Django 서버 주소


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
            .baseUrl("http://172.30.1.26:8000/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    data class ArtworkResponse(
        val title: String,
        val artist: String,
        val year: Int,
        val style: String,
        val description: String
    )
}
