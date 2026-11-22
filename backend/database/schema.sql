-- =========================================================
-- SubTrack - Skema Database
-- Membuat database subtrack_db beserta 6 tabel yang saling berelasi
-- =========================================================

DROP DATABASE IF EXISTS subtrack_db;
CREATE DATABASE subtrack_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE subtrack_db;

-- 1. users: akun pengguna (Admin dan User)
CREATE TABLE users (
  id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
  name               VARCHAR(50)  NOT NULL,
  email              VARCHAR(100) NOT NULL UNIQUE,
  password_hash      VARCHAR(255) NOT NULL,
  role               ENUM('ADMIN','USER') NOT NULL DEFAULT 'USER',
  refresh_token_hash VARCHAR(255) NULL,
  refresh_expires_at DATETIME     NULL,
  created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. digital_services: katalog layanan, dikelola Admin
CREATE TABLE digital_services (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  created_by  BIGINT       NOT NULL,
  name        VARCHAR(50)  NOT NULL UNIQUE,
  category    ENUM('STREAMING_VIDEO','MUSIK','CLOUD_STORAGE','PRODUKTIVITAS','GAMING','LAINNYA') NOT NULL,
  description VARCHAR(255) NULL,
  website_url VARCHAR(255) NULL,
  is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_services_creator FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB;

-- 3. subscriptions: langganan milik User
CREATE TABLE subscriptions (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id           BIGINT        NOT NULL,
  service_id        BIGINT        NOT NULL,
  plan_name         VARCHAR(50)   NOT NULL,
  price             DECIMAL(12,0) NOT NULL,
  billing_cycle     ENUM('BULANAN','TRIWULAN','TAHUNAN') NOT NULL,
  start_date        DATE          NOT NULL,
  next_billing_date DATE          NOT NULL,
  payment_method    VARCHAR(30)   NULL,
  status            ENUM('ACTIVE','PAUSED','CANCELLED') NOT NULL DEFAULT 'ACTIVE',
  notes             VARCHAR(255)  NULL,
  created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_subs_user    FOREIGN KEY (user_id)    REFERENCES users(id)            ON DELETE CASCADE,
  CONSTRAINT fk_subs_service FOREIGN KEY (service_id) REFERENCES digital_services(id) ON DELETE RESTRICT,
  CONSTRAINT chk_subs_price  CHECK (price > 0)
) ENGINE=InnoDB;

-- 4. payments: catatan pembayaran per langganan
CREATE TABLE payments (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  subscription_id BIGINT        NOT NULL,
  amount          DECIMAL(12,0) NOT NULL,
  paid_at         DATE          NOT NULL,
  method          VARCHAR(30)   NULL,
  proof_file_name VARCHAR(100)  NULL,
  note            VARCHAR(255)  NULL,
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_pay_sub      FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE,
  CONSTRAINT chk_pay_amount  CHECK (amount > 0)
) ENGINE=InnoDB;

-- 5. shared_groups: grup patungan, pemiliknya User
CREATE TABLE shared_groups (
  id              BIGINT AUTO_INCREMENT PRIMARY KEY,
  owner_id        BIGINT        NOT NULL,
  service_id      BIGINT        NOT NULL,
  subscription_id BIGINT        NULL,
  name            VARCHAR(50)   NOT NULL,
  total_cost      DECIMAL(12,0) NOT NULL,
  billing_cycle   ENUM('BULANAN','TRIWULAN','TAHUNAN') NOT NULL,
  due_day         TINYINT       NOT NULL,
  max_members     TINYINT       NOT NULL,
  description     VARCHAR(255)  NULL,
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_groups_owner   FOREIGN KEY (owner_id)        REFERENCES users(id)            ON DELETE CASCADE,
  CONSTRAINT fk_groups_service FOREIGN KEY (service_id)      REFERENCES digital_services(id) ON DELETE RESTRICT,
  CONSTRAINT fk_groups_sub     FOREIGN KEY (subscription_id) REFERENCES subscriptions(id)    ON DELETE SET NULL,
  CONSTRAINT chk_groups_cost   CHECK (total_cost > 0),
  CONSTRAINT chk_groups_due    CHECK (due_day BETWEEN 1 AND 28),
  CONSTRAINT chk_groups_max    CHECK (max_members BETWEEN 2 AND 10)
) ENGINE=InnoDB;

-- 6. group_members: anggota grup dan status iurannya
CREATE TABLE group_members (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY,
  group_id     BIGINT        NOT NULL,
  member_name  VARCHAR(50)   NOT NULL,
  contact      VARCHAR(100)  NULL,
  share_amount DECIMAL(12,0) NOT NULL,
  dues_status  ENUM('LUNAS','BELUM_LUNAS') NOT NULL DEFAULT 'BELUM_LUNAS',
  paid_at      DATE          NULL,
  note         VARCHAR(255)  NULL,
  created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_members_group FOREIGN KEY (group_id) REFERENCES shared_groups(id) ON DELETE CASCADE,
  CONSTRAINT chk_members_share CHECK (share_amount > 0)
) ENGINE=InnoDB;
