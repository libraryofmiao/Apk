package com.library.membership.api

// Full member record, as returned by /api/member, /api/member-basic, /api/verify
data class Member(
    val id: Any? = null,
    val memberId: String? = null,
    val fullName: String? = null,
    val guardian: String? = null,
    val gender: String? = null,
    val dob: String? = null,
    val occupation: String? = null,
    val address: String? = null,
    val district: String? = null,
    val state: String? = null,
    val pincode: String? = null,
    val mobile: String? = null,
    val email: String? = null,
    val membershipType: String? = null,
    val duration: String? = null,
    val idType: String? = null,
    val idNumber: String? = null,
    val issueDate: String? = null,
    val status: String? = null,
    val photoUrl: String? = null,
    val timestamp: String? = null,
    val verify: String? = null,
    val photoKey: String? = null
)

// Slim row shown in the /api/members list
data class MemberSummary(
    val id: Any? = null,
    val memberId: String = "",
    val fullName: String = "",
    val mobile: String = "",
    val email: String? = null,
    val membershipType: String? = null,
    val status: String? = null,
    val verify: String? = null,
    val photoKey: String? = null
)

data class MembersResponse(
    val success: Boolean,
    val members: List<MemberSummary> = emptyList(),
    val message: String? = null
)

data class MemberResponse(
    val success: Boolean,
    val member: Member? = null,
    val message: String? = null
)

data class RegisterResponse(
    val success: Boolean,
    val memberId: String? = null,
    val verify: String? = null,
    val photoKey: String? = null,
    val recordId: Any? = null,
    val message: String? = null
)

data class UpdateResponse(
    val success: Boolean,
    val message: String? = null,
    val verify: String? = null,
    val oldVerify: String? = null,
    val photoKey: String? = null
)

data class StatusUpdateResponse(
    val success: Boolean,
    val memberId: String? = null,
    val status: String? = null,
    val message: String? = null
)

data class SimpleResponse(
    val success: Boolean,
    val message: String? = null
)

data class StatusUpdateRequest(val memberId: String, val status: String)

data class DeleteMemberBody(val memberId: String)
