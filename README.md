# SubTrack

**SubTrack: Aplikasi Pencatat Langganan Digital dan Pengelola Patungan Berbasis Android Menggunakan Jetpack Compose**

Project Akhir Pengembangan Aplikasi Mobile (PAM) 2026 (individu).

| | |
|---|---|
| Nama | Pasya Achmadinedja Maulana |
| NIM | 20240140035 |
| Kelas | B |
| Repository API | https://github.com/PasyaMaulana/0035_API_SubTrack |

SubTrack membantu pengguna mencatat langganan digital (streaming, musik, cloud, dan lainnya), memantau tagihan yang akan datang, menyimpan riwayat pembayaran beserta foto bukti, serta mengelola patungan langganan bersama teman atau keluarga.

## 1. Fitur

### Pengguna (User)

| Fitur | Keterangan |
|---|---|
| Autentikasi | Register, login, sesi tersimpan, auto refresh token, logout |
| Dashboard | Estimasi pengeluaran per bulan, total dibayar bulan ini, pengeluaran per kategori, tagihan 14 hari ke depan, ringkasan iuran patungan |
| Langganan | Daftar dengan pencarian dan filter status, detail, tambah, ubah, hapus. Tanggal tagihan berikutnya bisa dihitung otomatis oleh server |
| Catatan Pembayaran | Riwayat dan total pembayaran per langganan, tambah, ubah, hapus |
| Foto Bukti | Foto bukti pembayaran dipilih lewat Photo Picker dan disimpan di penyimpanan lokal perangkat |
| Grup Patungan | Daftar grup dengan progres iuran, detail, tambah, ubah, hapus, dan bisa ditautkan ke langganan |
| Anggota dan Iuran | Daftar anggota, tambah, ubah, hapus, tandai lunas atau belum lunas. Iuran otomatis dibagi rata |
| Ekspor Laporan | Daftar langganan dan riwayat pembayaran diekspor ke file CSV lalu dibagikan |
| Profil | Data akun dan logout |

### Admin

| Fitur | Keterangan |
|---|---|
| Dashboard | Total pengguna, layanan, langganan, grup, dan layanan terpopuler |
| Katalog Layanan | Daftar dengan pencarian dan filter kategori, tambah, ubah, nonaktifkan, hapus |
| Profil | Data akun dan logout |

## 2. Teknologi

| Kebutuhan | Teknologi |
|---|---|
| Bahasa dan UI | Kotlin, Jetpack Compose (Material 3), tanpa layout XML |
| Arsitektur | MVVM dengan Repository dan manual dependency injection |
| Navigasi | Navigation Compose |
| Koneksi API | Retrofit 3, OkHttp (interceptor token dan authenticator untuk auto refresh), kotlinx.serialization |
| Penyimpanan sesi | DataStore Preferences |
| Gambar | Coil (`AsyncImage`) dan Photo Picker |
| Berbagi file | FileProvider dan `Intent.ACTION_SEND` |
| Minimum SDK | API 26 (Android 8.0), target SDK 37 |

Semua teks, ukuran, dan warna disimpan di resource (`strings.xml`, `dimens.xml`, `colors.xml`, dan tema), tanpa nilai yang ditulis langsung di kode.

## 3. Struktur Project

```
app/src/main/java/com/example/subtrack/
├── SubTrackApplication.kt   class Application, membuat AppContainer
├── MainActivity.kt
├── di/                      AppContainer (manual dependency injection)
├── model/                   MODEL
│   ├── dto/                 data class API dan enum
│   ├── network/             interface Retrofit, AuthInterceptor, TokenAuthenticator
│   ├── local/               SessionManager (DataStore)
│   └── repository/          repository per fitur
├── viewmodel/               VIEWMODEL, dikelompokkan per fitur
│   └── auth/ katalog/ langganan/ pembayaran/ grup/ anggota/ dashboard/ profil/
├── view/                    VIEW (Composable), dikelompokkan per fitur
│   ├── auth/ katalog/ langganan/ pembayaran/ grup/ anggota/ dashboard/ profil/
│   ├── components/          komponen reusable
│   ├── navigation/          NavGraph dan destinasi
│   └── theme/               tema aplikasi
└── util/                    format rupiah dan tanggal, foto bukti, ekspor CSV
```

## 4. Cara Menjalankan

### Prasyarat

- Android Studio versi terbaru
- Backend dari repository API sudah berjalan (lihat README repository API)

### Langkah

1. Clone repository ini, lalu buka foldernya di Android Studio (**Open**).
2. Tunggu Gradle sync selesai.
3. Jalankan backend: `npm run dev` di folder repository API.
4. Jalankan aplikasi di emulator.

Secara default aplikasi menghubungi `http://10.0.2.2:3000/api/v1/`, yaitu alamat laptop dari dalam emulator.

### Menjalankan di HP Android

1. Pastikan HP dan laptop terhubung ke Wi-Fi yang sama.
2. Cari IP laptop dengan `ipconfig` (Windows), misalnya `192.168.1.10`.
3. Tambahkan baris berikut di file `local.properties`, lalu sync Gradle:
   ```
   api.base.url=http://192.168.1.10:3000/api/v1/
   ```
4. Izinkan Node.js di Windows Firewall bila diminta.
5. Di Android 17 ke atas, izinkan akses jaringan lokal saat aplikasi pertama kali dibuka.

## 5. Akun Demo

| Role | Email | Password |
|---|---|---|
| Admin | admin@subtrack.id | Admin123 |
| User | user@subtrack.id | User1234 |

## 6. Status Fitur

| Fitur | Status |
|---|---|
| Autentikasi dan navigasi per role | ✅ Selesai |
| Katalog Layanan | ✅ Selesai |
| Langganan | ✅ Selesai |
| Catatan Pembayaran | ✅ Selesai |
| Foto bukti di penyimpanan lokal | ✅ Selesai |
| Grup Patungan | ✅ Selesai |
| Anggota dan Iuran | ✅ Selesai |
| Dashboard | ✅ Selesai |
| Ekspor laporan CSV | ✅ Selesai |
| Profil dan logout | ✅ Selesai |

## 7. Progress Mingguan (Weekly Progress)

Berikut linimasa pengembangan yang disusun dari riwayat git commit:

### 📅 Minggu 1 (12 November 2026)

- **Inisialisasi Repository dan Project** (`cac1c5f`, `3823cf0`, `5589d3e`): struktur folder, project Android Jetpack Compose, serta README awal dengan tabel status fitur. Dikerjakan pada 22 November 2026 (commit minggu 1 terlambat dengan izin dosen).

### 📅 Minggu 2 (19 - 25 November 2026)

- **Pengembangan Backend (awalnya satu repository)** (`c50240b`, `6af606e`, `4a4082d`, `dc6f9de`, `d913844`, `062a949`, `b662d35`, `8b7c068`, `c926ce1`): skema database 6 tabel, setup Express dan Sequelize, migration, model, seeder, dan panduan menjalankan backend. Bagian ini kemudian dipindahkan ke repository API.

### 📅 Minggu 3 (26 November - 2 Desember 2026)

- **Pemisahan Repository** (`997bd05`, `e91a32d`): backend dipindahkan ke repository API, project Android dipindahkan ke root repository ini, dan README diperbarui.
- **Fondasi Aplikasi** (`2f1f2cd`, `f6a627a`, `9603a9e`): dependency (Retrofit, OkHttp, Navigation, DataStore, Coil), konfigurasi alamat server, resource string, dimensi, warna, tema, model data, dan service API.
- **Sesi dan Autentikasi** (`5d9fb3a`, `fbd062c`, `aed5b10`): penyimpanan sesi di DataStore, klien Retrofit dengan auto refresh token, repository dan ViewModel autentikasi, serta layar login dan register dengan validasi dan izin jaringan lokal.
- **Arsitektur dan Navigasi** (`5b680ff`, `9d46ed8`, `f923cc6`): penyusunan ulang package sesuai lapisan MVVM, navigasi per role dengan cek sesi otomatis, serta komponen UI reusable dan utilitas format rupiah dan tanggal.
- **Katalog Layanan** (`e851425`): pencarian, filter kategori, dan pengelolaan katalog untuk Admin.
- **Langganan dan Pembayaran** (`124b3d0`, `d7f08ec`, `883f5af`): daftar, detail, form tambah dan ubah langganan dengan input rupiah, serta catatan pembayaran dengan foto bukti di penyimpanan lokal.
- **Grup Patungan dan Anggota** (`8b56519`, `340f82e`): grup patungan dengan ringkasan dan progres iuran, serta pengelolaan anggota dan status lunas.
- **Dashboard, Ekspor, dan Profil** (`bc95e71`, `99f0a09`, `5df5576`): dashboard berbeda untuk User dan Admin, ekspor laporan langganan dan pembayaran ke CSV, serta halaman profil dan logout.