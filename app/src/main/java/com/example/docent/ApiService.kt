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
import okhttp3.RequestBody

interface ApiService {
    @POST("auth/register/")
    fun registerUser(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("auth/login/")
    fun loginUser(@Body request: LoginRequest): Call<LoginResponse>

    @Multipart
    @POST("artworks/upload/") // Django 서버의 이미지 업로드 엔드포인트
    fun uploadArtwork(
        @Part image: MultipartBody.Part,
        @Part("user_id") userId: RequestBody,
        @Part("exhibition_id") exhibitionId: RequestBody
    ): Call<RetrofitClient.ArtworkResponse>

    @Streaming
    @POST("api/chat/") // SSE 지원하는 단일 API
    fun sendChatMessage(@Body payload: JsonObject): Call<ResponseBody>

    // 사용자 취향 분석 API
    @POST("artworks/analyze-preference/")
    fun analyzePreference(
        @Header("Authorization") token: String,
        @Body request: PreferenceRequest
    ): Call<PreferenceResponse>


    @GET("artworks/random/")
    fun getRandomArtworks(@Header("Authorization") token: String): Call<List<Artwork>>

    @GET("api/exhibition/")
    fun getExhibitions(): Call<List<Exhibition>>

    @GET("api/reviews/reviews/")
    fun getReviewsByExhibition(@Query("exhibition") exhibitionId: Int): Call<List<Review>>

    @POST("api/reviews/reviews/")
    fun postReview(
        @Header("Authorization") token: String,
        @Body reviewRequest: ReviewRequest
    ): Call<Review>


    @Multipart
    @PATCH("auth/update-profile/")
    fun updateProfile(
        @Header("Authorization") token: String,
        @Part profileImage: MultipartBody.Part?,
        @Part("nickname") nickname: RequestBody
    ): Call<ResponseBody>

    @GET("auth/user-info/")
    fun getUserInfo(
        @Header("Authorization") token: String
    ): Call<UserInfoResponse>

    // 좋아요 토글 API
    @POST("api/exhibition/toggle-like/")
    fun toggleLike(@Body body: JsonObject): Call<JsonObject>

    @GET("api/exhibition/sorted-user/")
    fun getSortedExhibitions(@Query("user_id") userId: Int): Call<List<Exhibition>>

    @GET("api/artworks/viewinghistory/{user_id}")
    fun getViewedExhibitions(@Path("user_id") userId: Int): Call<List<ViewedExhibition>>

    @GET("api/exhibition/search/")
    fun searchExhibitions(@Query("q") query: String): Call<ExhibitionSearchResponse>


}


