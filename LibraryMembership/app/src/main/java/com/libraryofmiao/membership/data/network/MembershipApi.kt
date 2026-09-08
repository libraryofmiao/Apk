package com.libraryofmiao.membership.data.network

import com.libraryofmiao.membership.data.model.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Talks to the Worker deployed at library-membership-system.libraryofmiao.workers.dev/api.
 * Every route here mirrors one already confirmed live in worker.js.
 */
interface MembershipApi {

    @GET("health")
    suspend fun health(): Response<GenericApiResponse>

    @GET("members")
    suspend fun listMembers(): Response<MembersListResponse>

    @GET("member")
    suspend fun getMember(@Query("memberId") memberId: String): Response<MemberResponse>

    @Multipart
    @POST("register")
    suspend fun register(
        @PartMap fields: Map<String, @JvmSuppressWildcards okhttp3.RequestBody>,
        @Part photo: MultipartBody.Part?
    ): Response<MemberResponse>

    @Multipart
    @PATCH("member")
    suspend fun updateMember(
        @PartMap fields: Map<String, @JvmSuppressWildcards okhttp3.RequestBody>,
        @Part photo: MultipartBody.Part?
    ): Response<MemberResponse>

    @PATCH("member-status")
    suspend fun updateStatus(@Body body: Map<String, String>): Response<GenericApiResponse>

    @DELETE("member")
    suspend fun deleteMember(@Query("memberId") memberId: String): Response<GenericApiResponse>

    @GET("verify")
    suspend fun verify(@Query("verify") verifyCode: String): Response<VerifyResult>

    @GET("photo")
    @Streaming
    suspend fun photo(
        @Query("memberId") memberId: String,
        @Query("verify") verify: String? = null,
        @Query("photoKey") photoKey: String? = null
    ): Response<ResponseBody>

    /** Admin-only. Requires X-Option-Admin-Key header — see AuthedOptionsApi below. */
    @POST("options")
    suspend fun updateOptions(
        @Header("X-Option-Admin-Key") adminKey: String,
        @Body body: OptionSets
    ): Response<GenericApiResponse>
}

/** Separate small interface for the Pages Function login endpoint (pages.dev domain, not workers.dev). */
interface AuthApi {
    @POST("api/login")
    suspend fun login(@Body body: Map<String, String>): Response<GenericApiResponse>
}
