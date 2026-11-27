package com.example.subtrack.apiservice

import com.example.subtrack.modeldata.ApiResponse
import com.example.subtrack.modeldata.Group
import com.example.subtrack.modeldata.GroupRequest
import com.example.subtrack.modeldata.UpdateGroupRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// CRUD grup patungan. Response menyertakan service, subscription, dan summary.
interface GroupApi {
    @GET("groups")
    suspend fun getGroups(): ApiResponse<List<Group>>

    @GET("groups/{id}")
    suspend fun getGroup(@Path("id") id: Int): ApiResponse<Group>

    @POST("groups")
    suspend fun createGroup(@Body body: GroupRequest): ApiResponse<Group>

    @PUT("groups/{id}")
    suspend fun updateGroup(@Path("id") id: Int, @Body body: UpdateGroupRequest): ApiResponse<Group>

    @DELETE("groups/{id}")
    suspend fun deleteGroup(@Path("id") id: Int): ApiResponse<Unit>
}
