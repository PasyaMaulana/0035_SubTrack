package com.example.subtrack.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Anggota grup patungan beserta status iurannya
@Serializable
data class Member(
    val id: Int,
    @SerialName("group_id") val groupId: Int,
    @SerialName("member_name") val memberName: String,
    val contact: String? = null,
    @SerialName("share_amount") val shareAmount: Long = 0,
    @SerialName("dues_status") val duesStatus: DuesStatus = DuesStatus.BELUM_LUNAS,
    @SerialName("paid_at") val paidAt: String? = null,
    val note: String? = null
)

// Body POST anggota. share_amount null = dibagi rata oleh server.
@Serializable
data class MemberRequest(
    @SerialName("member_name") val memberName: String,
    val contact: String? = null,
    @SerialName("share_amount") val shareAmount: Long? = null,
    @SerialName("dues_status") val duesStatus: DuesStatus = DuesStatus.BELUM_LUNAS,
    @SerialName("paid_at") val paidAt: String? = null,
    val note: String? = null
)

// Body PUT anggota. Semua field opsional: hanya field yang diisi (bukan null) yang dikirim.
@Serializable
data class UpdateMemberRequest(
    @SerialName("member_name") val memberName: String? = null,
    val contact: String? = null,
    @SerialName("share_amount") val shareAmount: Long? = null,
    @SerialName("dues_status") val duesStatus: DuesStatus? = null,
    @SerialName("paid_at") val paidAt: String? = null,
    val note: String? = null
)
