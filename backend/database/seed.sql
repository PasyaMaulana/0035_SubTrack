-- =========================================================
-- SubTrack - Data Awal (Seed)
-- Jalankan SETELAH 01_schema.sql
-- Akun demo:  admin@subtrack.id / Admin123   (ADMIN)
--             user@subtrack.id  / User1234   (USER)
-- =========================================================

USE subtrack_db;

-- Akun (password sudah di-hash dengan bcrypt, cost 10)
INSERT INTO users (id, name, email, password_hash, role) VALUES
(1, 'Admin SubTrack', 'admin@subtrack.id', '$2b$10$8MAHCtwopWcLm9CRL6RbP.s3Icy4NG23ct1FAuXwjJ07Rju2NnDkm', 'ADMIN'),
(2, 'Pasya Demo',     'user@subtrack.id',  '$2b$10$y/ju24k0qS9KhIAl/8rCfekXUv6gxaXF/rAlRPr4lQ0L2cuRmn6lG', 'USER');

-- Katalog layanan (dibuat oleh Admin, id 1)
INSERT INTO digital_services (id, created_by, name, category, description, website_url) VALUES
(1,  1, 'Netflix',          'STREAMING_VIDEO', 'Layanan streaming film dan serial',        'https://www.netflix.com'),
(2,  1, 'Disney+ Hotstar',  'STREAMING_VIDEO', 'Streaming film Disney, Marvel, dan lokal',  'https://www.hotstar.com'),
(3,  1, 'YouTube Premium',  'STREAMING_VIDEO', 'YouTube tanpa iklan dan putar di latar',    'https://www.youtube.com/premium'),
(4,  1, 'Spotify',          'MUSIK',           'Streaming musik dan podcast',               'https://www.spotify.com'),
(5,  1, 'Apple Music',      'MUSIK',           'Streaming musik dari Apple',                'https://www.apple.com/apple-music'),
(6,  1, 'Google One',       'CLOUD_STORAGE',   'Penyimpanan cloud Google',                  'https://one.google.com'),
(7,  1, 'iCloud+',          'CLOUD_STORAGE',   'Penyimpanan cloud Apple',                   'https://www.icloud.com'),
(8,  1, 'Microsoft 365',    'PRODUKTIVITAS',   'Word, Excel, PowerPoint, dan OneDrive',     'https://www.microsoft.com/microsoft-365'),
(9,  1, 'Canva Pro',        'PRODUKTIVITAS',   'Desain grafis dengan fitur premium',        'https://www.canva.com'),
(10, 1, 'Xbox Game Pass',   'GAMING',          'Akses katalog game Xbox dan PC',            'https://www.xbox.com/xbox-game-pass');

-- Langganan milik user demo (id 2)
INSERT INTO subscriptions (id, user_id, service_id, plan_name, price, billing_cycle, start_date, next_billing_date, payment_method, status, notes) VALUES
(1, 2, 1, 'Premium',     186000, 'BULANAN',  '2026-08-05', '2026-12-05', 'E-wallet',      'ACTIVE', 'Dipakai patungan keluarga'),
(2, 2, 4, 'Family',       86900, 'BULANAN',  '2026-09-12', '2026-12-12', 'Kartu debit',   'ACTIVE', NULL),
(3, 2, 6, '100 GB',      269000, 'TAHUNAN',  '2026-01-20', '2027-01-20', 'Transfer bank', 'ACTIVE', 'Backup foto'),
(4, 2, 9, 'Pro',          95000, 'BULANAN',  '2026-06-01', '2026-12-01', 'E-wallet',      'PAUSED', 'Dijeda sementara');

-- Riwayat pembayaran
INSERT INTO payments (subscription_id, amount, paid_at, method, note) VALUES
(1, 186000, '2026-09-05', 'E-wallet',      NULL),
(1, 186000, '2026-10-05', 'E-wallet',      NULL),
(1, 186000, '2026-11-05', 'E-wallet',      NULL),
(2,  86900, '2026-10-12', 'Kartu debit',   NULL),
(2,  86900, '2026-11-12', 'Kartu debit',   NULL),
(3, 269000, '2026-01-20', 'Transfer bank', 'Pembayaran tahunan');

-- Grup patungan milik user demo, terkait langganan Netflix
INSERT INTO shared_groups (id, owner_id, service_id, subscription_id, name, total_cost, billing_cycle, due_day, max_members, description) VALUES
(1, 2, 1, 1, 'Netflix Keluarga', 186000, 'BULANAN', 5, 4, 'Patungan Netflix Premium 4 orang'),
(2, 2, 4, 2, 'Spotify Family',    86900, 'BULANAN', 12, 6, 'Patungan Spotify Family');

-- Anggota grup
INSERT INTO group_members (group_id, member_name, contact, share_amount, dues_status, paid_at) VALUES
(1, 'Pasya',  NULL,               46500, 'LUNAS',       '2026-11-03'),
(1, 'Rafi',   'rafi@example.com', 46500, 'LUNAS',       '2026-11-04'),
(1, 'Nadia',  '081234567890',     46500, 'BELUM_LUNAS', NULL),
(1, 'Dimas',  NULL,               46500, 'BELUM_LUNAS', NULL),
(2, 'Pasya',  NULL,               14500, 'LUNAS',       '2026-11-10'),
(2, 'Salsa',  'salsa@example.com',14500, 'LUNAS',       '2026-11-11'),
(2, 'Bagas',  NULL,               14500, 'BELUM_LUNAS', NULL);
