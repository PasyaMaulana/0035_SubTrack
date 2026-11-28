package com.example.subtrack.model.repository

import com.example.subtrack.model.dto.Member
import com.example.subtrack.model.dto.MemberRequest
import com.example.subtrack.model.dto.UpdateMemberRequest
import com.example.subtrack.model.network.MemberApi
import kotlinx.serialization.json.Json

// Kontrak repository anggota grup patungan. Semua operasi berada di bawah satu grup.
interface MemberRepository {
    // Diurutkan server berdasarkan nama anggota
    suspend fun daftarAnggota(idGrup: Int): HasilApi<List<Member>>

    suspend fun detailAnggota(idGrup: Int, idAnggota: Int): HasilApi<Member>

    // share_amount null = server membagi rata total biaya. Grup penuh -> 409.
    suspend fun tambahAnggota(idGrup: Int, request: MemberRequest): HasilApi<Member>

    // Field null tidak diubah; untuk mengosongkan teks kirim "".
    // Dipakai juga untuk menandai LUNAS / BELUM_LUNAS (server menyelaraskan paid_at).
    suspend fun ubahAnggota(idGrup: Int, idAnggota: Int, request: UpdateMemberRequest): HasilApi<Member>

    suspend fun hapusAnggota(idGrup: Int, idAnggota: Int): HasilApi<Unit>
}

class NetworkMemberRepository(
    private val memberApi: MemberApi,
    private val json: Json
) : MemberRepository {

    override suspend fun daftarAnggota(idGrup: Int): HasilApi<List<Member>> =
        panggilApi(json) { memberApi.getMembers(idGrup) }

    override suspend fun detailAnggota(idGrup: Int, idAnggota: Int): HasilApi<Member> =
        panggilApi(json) { memberApi.getMember(idGrup, idAnggota) }

    override suspend fun tambahAnggota(idGrup: Int, request: MemberRequest): HasilApi<Member> =
        panggilApi(json) { memberApi.createMember(idGrup, request) }

    override suspend fun ubahAnggota(idGrup: Int, idAnggota: Int, request: UpdateMemberRequest): HasilApi<Member> =
        panggilApi(json) { memberApi.updateMember(idGrup, idAnggota, request) }

    override suspend fun hapusAnggota(idGrup: Int, idAnggota: Int): HasilApi<Unit> =
        panggilApiTanpaData(json) { memberApi.deleteMember(idGrup, idAnggota) }
}
