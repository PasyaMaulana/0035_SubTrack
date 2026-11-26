# SubTrack

**SubTrack: Aplikasi Pencatat Langganan Digital dan Pengelola Patungan Berbasis Android Menggunakan Jetpack Compose**

Project Akhir Pengembangan Aplikasi Mobile (PAM) 2026 (individu).

| | |
|---|---|
| Nama | Pasya Achmadinedja Maulana |
| NIM | 20240140035 |
| Kelas | B |
| Repository API | https://github.com/PasyaMaulana/0035_API_SubTrack |

## 1. Teknologi

| Kebutuhan | Teknologi |
|---|---|
| Bahasa dan UI | Kotlin, Jetpack Compose (Material 3) |
| Arsitektur | MVVM (ViewModel dan StateFlow) |
| Navigasi | Navigation Compose |
| Koneksi API | Retrofit dan OkHttp |
| Penyimpanan token | DataStore |
| Gambar | Coil |
| Minimum SDK | API 26 |

## 2. Cara Menjalankan

1. Clone repository ini, lalu buka foldernya di Android Studio (**Open**).
2. Tunggu Gradle sync selesai.
3. Jalankan backend dari repository API (lihat README repository tersebut).
4. Jalankan aplikasi di emulator atau perangkat.

## 3. Status Fitur

| Fitur | Status |
|---|---|
| Autentikasi dan navigasi per role | ⏳ Belum |
| Katalog Layanan | ⏳ Belum |
| Langganan | ⏳ Belum |
| Catatan Pembayaran | ⏳ Belum |
| Grup Patungan | ⏳ Belum |
| Anggota dan Iuran | ⏳ Belum |
| Dashboard | ⏳ Belum |
| Foto bukti dan ekspor laporan | ⏳ Belum |

## 4. Progress Mingguan (Weekly Progress)

Berikut linimasa pengembangan yang disusun dari riwayat git commit:

### 📅 Minggu 1 (12 November 2026)

- **Inisialisasi Repository dan Project** (`cac1c5f`, `3823cf0`, `5589d3e`): struktur folder, project Android Jetpack Compose, serta README awal dengan tabel status fitur. Dikerjakan pada 22 November 2026 (commit minggu 1 terlambat dengan izin dosen).

### 📅 Minggu 2 (19 - 25 November 2026)

- **Pengembangan Backend (awalnya satu repository)** (`c50240b`, `6af606e`, `4a4082d`, `dc6f9de`, `d913844`, `062a949`, `b662d35`, `8b7c068`, `c926ce1`): skema database 6 tabel, setup Express dan Sequelize, migration, model, seeder, dan panduan menjalankan backend. Bagian ini kemudian dipindahkan ke repository API.

### 📅 Minggu 3 (26 November 2026)

- **Pemisahan Repository** (`997bd05`): backend dipindahkan ke repository API, dan project Android dipindahkan ke root repository ini.
