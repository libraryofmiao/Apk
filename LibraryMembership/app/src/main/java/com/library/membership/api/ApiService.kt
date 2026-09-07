package com.library.membership.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.Query

interface ApiService {

    @GET("api/members")
    suspend fun getMembers(): MembersResponse

    @GET("api/member")
    suspend fun getMember(@Query("memberId") memberId: String): MemberResponse

    @GET("api/member-basic")
    suspend fun getMemberBasic(@Query("memberId") memberId: String): MemberResponse

    @GET("api/verify")
    suspend fun verifyByCode(@Query("verify") verify: String): MemberResponse

    @retrofit2.http.POST("api/register")
    suspend fun register(@Body fields: Map<String, @JvmSuppressWildcards Any?>): RegisterResponse

    @PATCH("api/member")
    suspend fun updateMember(@Body fields: Map<String, @JvmSuppressWildcards Any?>): UpdateResponse

    @PATCH("api/member-status")
    suspend fun updateStatus(@Body body: StatusUpdateRequest): StatusUpdateResponse

    // Retrofit's @DELETE doesn't support a body by default; the Worker expects
    // memberId as a query param on DELETE, which matches handleDeleteMember().
    @DELETE("api/member")
    suspend fun deleteMember(@Query("memberId") memberId: String): SimpleResponse
}
