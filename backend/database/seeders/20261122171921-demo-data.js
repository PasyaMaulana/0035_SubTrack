'use strict';
const bcrypt = require('bcrypt');

/**
 * Data awal SubTrack.
 * Akun demo:  admin@subtrack.id / Admin123   (ADMIN)
 *             user@subtrack.id  / User1234   (USER)
 */

/** @type {import('sequelize-cli').Migration} */
module.exports = {
  async up(queryInterface) {
    const now = new Date();
    const ts = { created_at: now, updated_at: now };

    // 1. Akun (password di-hash dengan bcrypt, cost 10)
    await queryInterface.bulkInsert('users', [
      { id: 1, name: 'Admin SubTrack', email: 'admin@subtrack.id', password_hash: await bcrypt.hash('Admin123', 10), role: 'ADMIN', ...ts },
      { id: 2, name: 'Pasya Demo', email: 'user@subtrack.id', password_hash: await bcrypt.hash('User1234', 10), role: 'USER', ...ts },
    ]);

    // 2. Katalog layanan (dibuat oleh Admin, id 1)
    const services = [
      ['Netflix', 'STREAMING_VIDEO', 'Layanan streaming film dan serial', 'https://www.netflix.com'],
      ['Disney+ Hotstar', 'STREAMING_VIDEO', 'Streaming film Disney, Marvel, dan lokal', 'https://www.hotstar.com'],
      ['YouTube Premium', 'STREAMING_VIDEO', 'YouTube tanpa iklan dan putar di latar', 'https://www.youtube.com/premium'],
      ['Spotify', 'MUSIK', 'Streaming musik dan podcast', 'https://www.spotify.com'],
      ['Apple Music', 'MUSIK', 'Streaming musik dari Apple', 'https://www.apple.com/apple-music'],
      ['Google One', 'CLOUD_STORAGE', 'Penyimpanan cloud Google', 'https://one.google.com'],
      ['iCloud+', 'CLOUD_STORAGE', 'Penyimpanan cloud Apple', 'https://www.icloud.com'],
      ['Microsoft 365', 'PRODUKTIVITAS', 'Word, Excel, PowerPoint, dan OneDrive', 'https://www.microsoft.com/microsoft-365'],
      ['Canva Pro', 'PRODUKTIVITAS', 'Desain grafis dengan fitur premium', 'https://www.canva.com'],
      ['Xbox Game Pass', 'GAMING', 'Akses katalog game Xbox dan PC', 'https://www.xbox.com/xbox-game-pass'],
    ];
    await queryInterface.bulkInsert(
      'digital_services',
      services.map(([name, category, description, website_url], i) => ({
        id: i + 1, created_by: 1, name, category, description, website_url, is_active: true, ...ts,
      }))
    );

    // 3. Langganan milik user demo (id 2)
    await queryInterface.bulkInsert('subscriptions', [
      { id: 1, user_id: 2, service_id: 1, plan_name: 'Premium', price: 186000, billing_cycle: 'BULANAN', start_date: '2026-08-05', next_billing_date: '2026-12-05', payment_method: 'E-wallet', status: 'ACTIVE', notes: 'Dipakai patungan keluarga', ...ts },
      { id: 2, user_id: 2, service_id: 4, plan_name: 'Family', price: 86900, billing_cycle: 'BULANAN', start_date: '2026-09-12', next_billing_date: '2026-12-12', payment_method: 'Kartu debit', status: 'ACTIVE', notes: null, ...ts },
      { id: 3, user_id: 2, service_id: 6, plan_name: '100 GB', price: 269000, billing_cycle: 'TAHUNAN', start_date: '2026-01-20', next_billing_date: '2027-01-20', payment_method: 'Transfer bank', status: 'ACTIVE', notes: 'Backup foto', ...ts },
      { id: 4, user_id: 2, service_id: 9, plan_name: 'Pro', price: 95000, billing_cycle: 'BULANAN', start_date: '2026-06-01', next_billing_date: '2026-12-01', payment_method: 'E-wallet', status: 'PAUSED', notes: 'Dijeda sementara', ...ts },
    ]);

    // 4. Riwayat pembayaran
    await queryInterface.bulkInsert('payments', [
      { subscription_id: 1, amount: 186000, paid_at: '2026-09-05', method: 'E-wallet', note: null, ...ts },
      { subscription_id: 1, amount: 186000, paid_at: '2026-10-05', method: 'E-wallet', note: null, ...ts },
      { subscription_id: 1, amount: 186000, paid_at: '2026-11-05', method: 'E-wallet', note: null, ...ts },
      { subscription_id: 2, amount: 86900, paid_at: '2026-10-12', method: 'Kartu debit', note: null, ...ts },
      { subscription_id: 2, amount: 86900, paid_at: '2026-11-12', method: 'Kartu debit', note: null, ...ts },
      { subscription_id: 3, amount: 269000, paid_at: '2026-01-20', method: 'Transfer bank', note: 'Pembayaran tahunan', ...ts },
    ]);

    // 5. Grup patungan milik user demo
    await queryInterface.bulkInsert('shared_groups', [
      { id: 1, owner_id: 2, service_id: 1, subscription_id: 1, name: 'Netflix Keluarga', total_cost: 186000, billing_cycle: 'BULANAN', due_day: 5, max_members: 4, description: 'Patungan Netflix Premium 4 orang', ...ts },
      { id: 2, owner_id: 2, service_id: 4, subscription_id: 2, name: 'Spotify Family', total_cost: 86900, billing_cycle: 'BULANAN', due_day: 12, max_members: 6, description: 'Patungan Spotify Family', ...ts },
    ]);

    // 6. Anggota grup
    await queryInterface.bulkInsert('group_members', [
      { group_id: 1, member_name: 'Pasya', contact: null, share_amount: 46500, dues_status: 'LUNAS', paid_at: '2026-11-03', ...ts },
      { group_id: 1, member_name: 'Rafi', contact: 'rafi@example.com', share_amount: 46500, dues_status: 'LUNAS', paid_at: '2026-11-04', ...ts },
      { group_id: 1, member_name: 'Nadia', contact: '081234567890', share_amount: 46500, dues_status: 'BELUM_LUNAS', paid_at: null, ...ts },
      { group_id: 1, member_name: 'Dimas', contact: null, share_amount: 46500, dues_status: 'BELUM_LUNAS', paid_at: null, ...ts },
      { group_id: 2, member_name: 'Pasya', contact: null, share_amount: 14500, dues_status: 'LUNAS', paid_at: '2026-11-10', ...ts },
      { group_id: 2, member_name: 'Salsa', contact: 'salsa@example.com', share_amount: 14500, dues_status: 'LUNAS', paid_at: '2026-11-11', ...ts },
      { group_id: 2, member_name: 'Bagas', contact: null, share_amount: 14500, dues_status: 'BELUM_LUNAS', paid_at: null, ...ts },
    ]);
  },

  async down(queryInterface) {
    // Hapus dari tabel anak ke induk supaya tidak bentrok dengan foreign key
    for (const table of ['group_members', 'shared_groups', 'payments', 'subscriptions', 'digital_services', 'users']) {
      await queryInterface.bulkDelete(table, null, {});
    }
  },
};
