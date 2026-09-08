package com.libraryofmiao.membership.data.model

import com.google.gson.annotations.SerializedName

/**
 * Mirrors the field set used by the deployed Worker (register.html / edit-member.html /
 * worker.js ALLOWED_FIELDS). Kept 1:1 with the live system so nothing is lost.
 */
data class Member(
    @SerializedName("memberId") val memberId: String? = null,   // e.g. SDLM0001
    @SerializedName("fullName") val fullName: String = "",
    @SerializedName("guardianName") val guardianName: String? = null,
    @SerializedName("gender") val gender: String? = null,        // Male / Female / Other
    @SerializedName("dob") val dob: String? = null,
    @SerializedName("occupation") val occupation: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("district") val district: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("pincode") val pincode: String? = null,
    @SerializedName("mobile") val mobile: String = "",
    @SerializedName("email") val email: String? = null,
    @SerializedName("membershipType") val membershipType: String? = null,
    @SerializedName("membershipDuration") val membershipDuration: String? = null,
    @SerializedName("idType") val idType: String? = null,
    @SerializedName("idNumber") val idNumber: String? = null,
    @SerializedName("status") val status: String = "Active",     // Active / Inactive
    @SerializedName("verify") val verify: String? = null,        // 6-char verification code
    @SerializedName("photoKey") val photoKey: String? = null,
    @SerializedName("issueDate") val issueDate: String? = null
)

/** Slimmed-down shape for the dashboard list — mirrors MEMBERS_LIST_FIELDS from worker.js. */
data class MemberSummary(
    @SerializedName("Id") val id: String? = null,
    @SerializedName("memberId") val memberId: String? = null,
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("mobile") val mobile: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("membershipType") val membershipType: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("verify") val verify: String? = null
)

data class MembersListResponse(
    val success: Boolean = true,
    val members: List<MemberSummary> = emptyList(),
    val stats: Stats? = null
) {
    data class Stats(val total: Int = 0)
}

data class MemberResponse(
    val success: Boolean = true,
    val member: Member? = null,
    val message: String? = null,
    val error: String? = null
)

data class GenericApiResponse(
    val success: Boolean = false,
    val message: String? = null,
    val error: String? = null
)

/** Minimal, verification-only shape — matches the "over-sharing" fix recommended for /api/verify. */
data class VerifyResult(
    val success: Boolean = false,
    val fullName: String? = null,
    val memberId: String? = null,
    val membershipType: String? = null,
    val status: String? = null,
    val photoKey: String? = null,
    val error: String? = null
)

/** Dropdown option sets, editable via the "Manage Options" admin panel (worker's /api/options). */
data class OptionSets(
    val membershipTypes: List<String> = listOf(
        "Student", "General", "Senior Citizen", "Research Scholar", "Faculty"
    ),
    val membershipDurations: List<String> = listOf(
        "Permanent ( Paid )", "Permanent ( Free of Cost )", "New Age Learning Centre"
    ),
    val idTypes: List<String> = listOf(
        "Aadhaar Card", "Voter ID", "PAN Card", "Driving License", "Passport", "Govt. ID"
    )
)
