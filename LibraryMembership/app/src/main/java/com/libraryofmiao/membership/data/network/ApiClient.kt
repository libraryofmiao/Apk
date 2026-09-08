package com.libraryofmiao.membership.data.network

import com.libraryofmiao.membership.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private val logging = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    val membershipApi: MembershipApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.WORKER_API_BASE.let { if (it.endsWith("/")) it else "$it/" })
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MembershipApi::class.java)
    }

    val authApi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.PAGES_BASE.let { if (it.endsWith("/")) it else "$it/" })
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }

    /** Full URL for loading a member/verify photo directly into Coil. */
    fun photoUrl(memberId: String, verify: String? = null, photoKey: String? = null): String {
        val base = BuildConfig.WORKER_API_BASE
        val sb = StringBuilder("$base/photo?memberId=$memberId")
        verify?.let { sb.append("&verify=$it") }
        photoKey?.let { sb.append("&photoKey=$it") }
        return sb.toString()
    }
}
