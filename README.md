# SubTrack

**SubTrack: Aplikasi Pencatat Langganan Digital dan Pengelola Patungan Berbasis Android Menggunakan Jetpack Compose**

SubTrack adalah aplikasi Android untuk mencatat dan memantau langganan digital serta iuran patungan langganan. Seluruh data dicatat manual oleh pengguna; aplikasi tidak memproses transaksi atau pembayaran apa pun.

**Pasya Achmadinedja Maulana – 20240140035 – Kelas B**
Project Akhir Pengembangan Aplikasi Mobile 2026

## Teknologi

| Bagian | Teknologi |
|---|---|
| Android | Kotlin, Jetpack Compose (Material 3), MVVM, Navigation Compose, Retrofit + OkHttp, DataStore, Coil |
| Backend | Node.js + Express, Sequelize (ORM) + sequelize-cli, Zod, JWT (Access & Refresh Token), bcrypt |
| Database | MySQL 8 |

## Struktur Repository

```
0035_SubTrack/
├── android/              # Project Android (Jetpack Compose)
├── backend/              # REST API Node.js + Express
│   ├── database/
│   │   ├── migrations/   # Migration Sequelize (skema tabel)
│   │   └── seeders/      # Seeder Sequelize (data awal)
│   └── src/              # Kode backend (config, models, routes, dll.)
└── README.md
```

## Status Fitur

| Fitur | Status |
|---|---|
| Autentikasi JWT (register, login, refresh token, logout) | ⏳ Belum |
| Hak akses berbasis role (Admin & User) | ⏳ Belum |
| CRUDS Katalog Layanan | ⏳ Belum |
| CRUDS Langganan | ⏳ Belum |
| CRUDS Catatan Pembayaran | ⏳ Belum |
| CRUDS Grup Patungan | ⏳ Belum |
| CRUDS Anggota dan Iuran | ⏳ Belum |
| Foto bukti pembayaran (penyimpanan lokal) | ⏳ Belum |
| Dashboard ringkasan | ⏳ Belum |
| Ekspor laporan CSV (penyimpanan lokal) | ⏳ Belum |

Keterangan: ✅ Selesai · 🔄 Dikerjakan · ⏳ Belum

## Cara Menjalankan

### Backend
1. Pastikan MySQL 8 sudah berjalan.
2. Masuk ke folder backend lalu install dependency:
   ```bash
   cd backend
   npm install
   ```
3. Salin `.env.example` menjadi `.env`, lalu isi `DB_PASSWORD` sesuai MySQL.
4. Buat database, tabel, dan data awal:
   ```bash
   npx sequelize-cli db:create --charset utf8mb4 --collate utf8mb4_unicode_ci
   npx sequelize-cli db:migrate
   npx sequelize-cli db:seed:all
   ```
5. Jalankan server:
   ```bash
   npm run dev
   ```
   Cek koneksi di `http://localhost:3000/api/v1/health`.

### Akun Demo

| Role | Email | Password |
|---|---|---|
| Admin | admin@subtrack.id | Admin123 |
| User | user@subtrack.id | User1234 |

### Aplikasi Android
Panduan akan ditambahkan setelah aplikasi Android terhubung ke backend.
