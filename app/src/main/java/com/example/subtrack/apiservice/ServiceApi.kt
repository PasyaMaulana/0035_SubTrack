package com.example.subtrack.apiservice

import com.example.subtrack.modeldata.ApiResponse
import com.example.subtrack.modeldata.Category
import com.example.subtrack.modeldata.Service
import com.example.subtrack.modeldata.ServiceRequest
import com.example.subtrack.modeldata.UpdateServiceRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

// Katalog layanan. Tambah/edit/hapus khusus ADMIN.
interface ServiceApi {
    // Parameter bernilai null tidak dikirim sebagai query
    @GET("services")
    suspend fun getServices(
        @Query("q") q: String? = null,
        @Query("category") category: Category? = null
    ): ApiResponse<List<Service>>

    @GET("services/{id}")
    suspend fun getService(@Path("id") id: Int): ApiResponse<Service>

    @POST("services")
    suspend fun createService(@Body body: ServiceRequest): ApiResponse<Service>

    @PUT("services/{id}")
    suspend fun updateService(@Path("id") id: Int, @Body body: UpdateServiceRequest): ApiResponse<Service>

    @DELETE("services/{id}")
    suspend fun deleteService(@Path("id") id: Int): ApiResponse<Unit>
}
