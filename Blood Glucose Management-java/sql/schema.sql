-- MySQL 8.0+ schema for the glucose-management API.
CREATE DATABASE IF NOT EXISTS bloodmanage CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bloodmanage;

CREATE TABLE IF NOT EXISTS users (
  id CHAR(36) PRIMARY KEY,
  name VARCHAR(60) NOT NULL,
  account VARCHAR(120) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  phone VARCHAR(30), diabetes_type VARCHAR(20) DEFAULT 'type2',
  target_min DECIMAL(5,1) NOT NULL DEFAULT 4.4, target_max DECIMAL(5,1) NOT NULL DEFAULT 7.8,
  doctor_name VARCHAR(60), doctor_clinic VARCHAR(120), timezone VARCHAR(50) NOT NULL DEFAULT 'Asia/Shanghai',
  active BOOLEAN NOT NULL DEFAULT TRUE, created_at DATETIME(6) NOT NULL, updated_at DATETIME(6) NOT NULL
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS auth_sessions (
  id CHAR(36) PRIMARY KEY, user_id CHAR(36) NOT NULL, refresh_token_hash VARCHAR(255) NOT NULL,
  device_name VARCHAR(120), expires_at DATETIME(6) NOT NULL, revoked BOOLEAN NOT NULL DEFAULT FALSE, created_at DATETIME(6) NOT NULL,
  CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES users(id), INDEX idx_session_token(refresh_token_hash)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS user_settings (
  user_id CHAR(36) PRIMARY KEY, glucose_reminder BOOLEAN NOT NULL DEFAULT TRUE, medication_reminder BOOLEAN NOT NULL DEFAULT TRUE,
  family_alert BOOLEAN NOT NULL DEFAULT TRUE, auto_sync BOOLEAN NOT NULL DEFAULT TRUE, face_id_unlock BOOLEAN NOT NULL DEFAULT FALSE,
  updated_at DATETIME(6) NOT NULL, CONSTRAINT fk_settings_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS glucose_records (
  id CHAR(36) PRIMARY KEY, user_id CHAR(36) NOT NULL, value DECIMAL(5,1) NOT NULL, unit VARCHAR(10) NOT NULL DEFAULT 'mmol/L',
  period VARCHAR(20) NOT NULL, measured_at DATETIME(6) NOT NULL, status VARCHAR(20) NOT NULL, note VARCHAR(500), deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME(6) NOT NULL, updated_at DATETIME(6) NOT NULL, CONSTRAINT fk_glucose_user FOREIGN KEY (user_id) REFERENCES users(id),
  INDEX idx_glucose_user_time(user_id, measured_at), INDEX idx_glucose_user_period(user_id, period)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS meal_records (
  id CHAR(36) PRIMARY KEY, user_id CHAR(36) NOT NULL, meal_type VARCHAR(20) NOT NULL, eaten_at DATETIME(6) NOT NULL,
  foods_json JSON NOT NULL, carbohydrate_grams DECIMAL(6,1), note VARCHAR(500), deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME(6) NOT NULL, updated_at DATETIME(6) NOT NULL, CONSTRAINT fk_meal_user FOREIGN KEY (user_id) REFERENCES users(id), INDEX idx_meal_user_time(user_id, eaten_at)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS medication_records (
  id CHAR(36) PRIMARY KEY, user_id CHAR(36) NOT NULL, medication_name VARCHAR(120) NOT NULL, dose DECIMAL(8,2) NOT NULL, dose_unit VARCHAR(20) NOT NULL,
  taken_at DATETIME(6), scheduled_at DATETIME(6), status VARCHAR(20) NOT NULL, note VARCHAR(500), deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME(6) NOT NULL, updated_at DATETIME(6) NOT NULL, CONSTRAINT fk_med_user FOREIGN KEY (user_id) REFERENCES users(id), INDEX idx_med_user_time(user_id, taken_at)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS exercise_records (
  id CHAR(36) PRIMARY KEY, user_id CHAR(36) NOT NULL, exercise_type VARCHAR(30) NOT NULL, started_at DATETIME(6) NOT NULL,
  duration_minutes INT NOT NULL, intensity VARCHAR(20) NOT NULL, before_glucose DECIMAL(5,1), after_glucose DECIMAL(5,1), note VARCHAR(500), deleted BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME(6) NOT NULL, updated_at DATETIME(6) NOT NULL, CONSTRAINT fk_exercise_user FOREIGN KEY (user_id) REFERENCES users(id), INDEX idx_exercise_user_time(user_id, started_at)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS family_connections (
  id CHAR(36) PRIMARY KEY, user_id CHAR(36) NOT NULL, contact VARCHAR(120) NOT NULL, relationship VARCHAR(30) NOT NULL,
  permissions_json VARCHAR(500) NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'pending', expires_at DATETIME(6) NOT NULL, created_at DATETIME(6) NOT NULL,
  CONSTRAINT fk_family_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS public_contents (
  id CHAR(36) PRIMARY KEY, slug VARCHAR(120) NOT NULL UNIQUE, content_type VARCHAR(20) NOT NULL, category VARCHAR(60) NOT NULL,
  title VARCHAR(200) NOT NULL, summary VARCHAR(500), content_lead VARCHAR(500), body LONGTEXT NOT NULL, cover_url VARCHAR(500), published_at DATETIME(6), published BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;
