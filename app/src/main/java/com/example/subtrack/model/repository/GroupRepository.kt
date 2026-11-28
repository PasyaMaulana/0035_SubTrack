package com.example.subtrack.model.repository

import com.example.subtrack.model.dto.Group
import com.example.subtrack.model.dto.GroupRequest
import com.example.subtrack.model.dto.UpdateGroupRequest
import com.example.subtrack.model.network.GroupApi
import kotlinx.serialization.json.Json

// Kontrak repository grup patungan milik user yang login
interface GroupRepository {
    // Setiap grup menyertakan service, subscription, dan summary iuran
    suspend fun daftarGrup(): HasilApi<List<Group>>

    suspend fun detailGrup(id: Int): HasilApi<Group>

    suspend fun tambahGrup(request: GroupRequest): HasilApi<Group>

    // Field null tidak diubah; untuk mengosongkan teks kirim ""
    suspend fun ubahGrup(id: Int, request: UpdateGroupRequest): HasilApi<Group>

    // Semua anggota grup ikut terhapus di server (CASCADE)
    suspend fun hapusGrup(id: Int): HasilApi<Unit>
}

class NetworkGroupRepository(
    private val groupApi: GroupApi,
    private val json: Json
) : GroupRepository {

    override suspend fun daftarGrup(): HasilApi<List<Group>> =
        panggilApi(json) { groupApi.getGroups() }

    override suspend fun detailGrup(id: Int): HasilApi<Group> =
        panggilApi(json) { groupApi.getGroup(id) }

    override suspend fun tambahGrup(request: GroupRequest): HasilApi<Group> =
        panggilApi(json) { groupApi.createGroup(request) }

    override suspend fun ubahGrup(id: Int, request: UpdateGroupRequest): HasilApi<Group> =
        panggilApi(json) { groupApi.updateGroup(id, request) }

    override suspend fun hapusGrup(id: Int): HasilApi<Unit> =
        panggilApiTanpaData(json) { groupApi.deleteGroup(id) }
}
