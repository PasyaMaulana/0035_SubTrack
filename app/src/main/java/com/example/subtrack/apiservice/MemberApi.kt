package com.example.subtrack.apiservice

import com.example.subtrack.modeldata.ApiResponse
import com.example.subtrack.modeldata.Member
import com.example.subtrack.modeldata.MemberRequest
import com.example.subtrack.modeldata.UpdateMemberRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// Anggota grup patungan, nested di bawah satu grup
interface MemberApi {
    @GET("groups/{groupId}/members")
    suspend fun getMembers(@Path("groupId") groupId: Int): ApiResponse<List<Member>>

    @GET("groups/{groupId}/members/{memberId}")
    suspend fun getMember(
        @Path("groupId") groupId: Int,
        @Path("memberId") memberId: Int
    ): ApiResponse<Member>

    @POST("groups/{groupId}/members")
    suspend fun createMember(
        @Path("groupId") groupId: Int,
        @Body body: MemberRequest
    ): ApiResponse<Member>

    @PUT("groups/{groupId}/members/{memberId}")
    suspend fun updateMember(
        @Path("groupId") groupId: Int,
        @Path("memberId") memberId: Int,
        @Body body: UpdateMemberRequest
    ): ApiResponse<Member>

    @DELETE("groups/{groupId}/members/{memberId}")
    suspend fun deleteMember(
        @Path("groupId") groupId: Int,
        @Path("memberId") memberId: Int
    ): ApiResponse<Unit>
}
