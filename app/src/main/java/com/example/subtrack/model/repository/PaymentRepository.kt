package com.example.subtrack.model.repository

import com.example.subtrack.model.dto.Payment
import com.example.subtrack.model.dto.PaymentList
import com.example.subtrack.model.dto.PaymentRequest
import com.example.subtrack.model.dto.UpdatePaymentRequest
import com.example.subtrack.model.network.PaymentApi
import kotlinx.serialization.json.Json

// Kontrak repository catatan pembayaran. Semua operasi berada di bawah satu langganan.
interface PaymentRepository {
    // data: total semua pembayaran dan daftarnya (terbaru dulu)
    suspend fun daftarPembayaran(idLangganan: Int): HasilApi<PaymentList>

    suspend fun detailPembayaran(idLangganan: Int, idPembayaran: Int): HasilApi<Payment>

    suspend fun tambahPembayaran(idLangganan: Int, request: PaymentRequest): HasilApi<Payment>

    // Field null tidak diubah; untuk mengosongkan teks (termasuk foto bukti) kirim ""
    suspend fun ubahPembayaran(
        idLangganan: Int,
        idPembayaran: Int,
        request: UpdatePaymentRequest
    ): HasilApi<Payment>

    suspend fun hapusPembayaran(idLangganan: Int, idPembayaran: Int): HasilApi<Unit>
}

class NetworkPaymentRepository(
    private val paymentApi: PaymentApi,
    private val json: Json
) : PaymentRepository {

    override suspend fun daftarPembayaran(idLangganan: Int): HasilApi<PaymentList> =
        panggilApi(json) { paymentApi.getPayments(idLangganan) }

    override suspend fun detailPembayaran(idLangganan: Int, idPembayaran: Int): HasilApi<Payment> =
        panggilApi(json) { paymentApi.getPayment(idLangganan, idPembayaran) }

    override suspend fun tambahPembayaran(idLangganan: Int, request: PaymentRequest): HasilApi<Payment> =
        panggilApi(json) { paymentApi.createPayment(idLangganan, request) }

    override suspend fun ubahPembayaran(
        idLangganan: Int,
        idPembayaran: Int,
        request: UpdatePaymentRequest
    ): HasilApi<Payment> =
        panggilApi(json) { paymentApi.updatePayment(idLangganan, idPembayaran, request) }

    override suspend fun hapusPembayaran(idLangganan: Int, idPembayaran: Int): HasilApi<Unit> =
        panggilApiTanpaData(json) { paymentApi.deletePayment(idLangganan, idPembayaran) }
}
