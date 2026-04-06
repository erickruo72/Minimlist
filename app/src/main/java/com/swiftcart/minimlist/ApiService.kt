package com.swiftcart.minimlist

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/register.php")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/login.php")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/verify_email.php")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): Response<VerifyEmailResponse>

    @POST("api/forgot_password.php")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<AuthResponse>

    companion object {
        private const val BASE_URL = "https://thehiringguide.com/"

        fun create(): ApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
