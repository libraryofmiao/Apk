package com.library.membership.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Points at your deployed library-membership-system Cloudflare Worker.
 */
object ApiClient {

    var BASE_URL: String = "https://library-membership-system.libraryofmiao.workers.dev/"
        private set

    private var retrofit: Retrofit? = null

    /** Call once at app startup (or from a settings screen) before first use. */
    fun configure(baseUrl: String) {
        BASE_URL = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        retrofit = null
    }

    val service: ApiService
        get() = (retrofit ?: buildRetrofit().also { retrofit = it }).create(ApiService::class.java)

    private fun buildRetrofit(): Retrofit {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /** Builds a direct image URL for the /api/photo endpoint (loadable via Coil). */
    fun photoUrl(memberId: String?, verify: String?, photoKey: String?): String? {
        if (!photoKey.isNullOrBlank()) {
            return "${BASE_URL}api/photo?photoKey=$photoKey"
        }
        if (!memberId.isNullOrBlank() && !verify.isNullOrBlank()) {
            return "${BASE_URL}api/photo?memberId=$memberId&verify=$verify"
        }
        return null
    }
}
