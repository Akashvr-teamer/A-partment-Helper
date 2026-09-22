-- ============================================
-- SMART APARTMENT SOCIETY MANAGEMENT SYSTEM
-- PostgreSQL Schema for Supabase ("A-partment" Schema)
-- ============================================

CREATE SCHEMA IF NOT EXISTS "A-partment";

SET search_path TO "A-partment";

-- Clean up any existing tables in this schema
DROP TABLE IF EXISTS "A-partment".payments CASCADE;
DROP TABLE IF EXISTS "A-partment".complaints CASCADE;
DROP TABLE IF EXISTS "A-partment".visitors CASCADE;
DROP TABLE IF EXISTS "A-partment".parking CASCADE;
DROP TABLE IF EXISTS "A-partment".workers CASCADE;
DROP TABLE IF EXISTS "A-partment".residents CASCADE;

-- ============================================
-- 1. RESIDENTS
-- ============================================

CREATE TABLE "A-partment".residents (
    resident_id SERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15),
    apartment_no VARCHAR(20) NOT NULL,
    password VARCHAR(255) NOT NULL,
    vehicle_no VARCHAR(20),
    parking_slot VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 2. WORKERS
-- ============================================

CREATE TABLE "A-partment".workers (
    worker_id SERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15),
    worker_type VARCHAR(50),
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 3. PARKING
-- ============================================

CREATE TABLE "A-partment".parking (
    parking_id SERIAL PRIMARY KEY,
    slot_number VARCHAR(10) UNIQUE NOT NULL,
    parking_type VARCHAR(20) NOT NULL CHECK (parking_type IN ('RESIDENT', 'VISITOR')),
    status VARCHAR(20) DEFAULT 'AVAILABLE' CHECK (status IN ('AVAILABLE', 'OCCUPIED')),
    resident_id INT NULL,
    visitor_id INT NULL,
    CONSTRAINT fk_parking_resident
        FOREIGN KEY (resident_id)
        REFERENCES "A-partment".residents(resident_id)
        ON DELETE SET NULL
);

-- ============================================
-- 4. VISITORS
-- ============================================

CREATE TABLE "A-partment".visitors (
    visitor_id SERIAL PRIMARY KEY,
    resident_id INT NOT NULL,
    visitor_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    has_vehicle BOOLEAN DEFAULT FALSE,
    vehicle_no VARCHAR(20),
    access_hash VARCHAR(64) NOT NULL,
    valid_from TIMESTAMP NOT NULL,
    valid_until TIMESTAMP NOT NULL,
    entry_time TIMESTAMP NULL,
    exit_time TIMESTAMP NULL,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ENTERED', 'EXITED', 'EXPIRED')),
    parking_slot VARCHAR(10) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_visitors_resident
        FOREIGN KEY (resident_id)
        REFERENCES "A-partment".residents(resident_id)
        ON DELETE CASCADE
);

-- Link parking visitor_id to visitors table
ALTER TABLE "A-partment".parking
    ADD CONSTRAINT fk_parking_visitor
    FOREIGN KEY (visitor_id)
    REFERENCES "A-partment".visitors(visitor_id)
    ON DELETE SET NULL;

-- ============================================
-- 5. COMPLAINTS / MAINTENANCE
-- ============================================

CREATE TABLE "A-partment".complaints (
    complaint_id SERIAL PRIMARY KEY,
    resident_id INT NOT NULL,
    worker_id INT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ASSIGNED', 'IN_PROGRESS', 'COMPLETED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    CONSTRAINT fk_complaints_resident
        FOREIGN KEY (resident_id)
        REFERENCES "A-partment".residents(resident_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_complaints_worker
        FOREIGN KEY (worker_id)
        REFERENCES "A-partment".workers(worker_id)
        ON DELETE SET NULL
);

-- ============================================
-- 6. PAYMENTS
-- ============================================

CREATE TABLE "A-partment".payments (
    payment_id SERIAL PRIMARY KEY,
    resident_id INT NOT NULL,
    month_year VARCHAR(20) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    due_date DATE NOT NULL,
    payment_date DATE NULL,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PAID', 'OVERDUE')),
    payment_method VARCHAR(30),
    transaction_id VARCHAR(100),
    CONSTRAINT fk_payments_resident
        FOREIGN KEY (resident_id)
        REFERENCES "A-partment".residents(resident_id)
        ON DELETE CASCADE
);

-- ============================================
-- SAMPLE SEED DATA
-- ============================================

INSERT INTO "A-partment".residents (full_name, email, phone, apartment_no, password, vehicle_no, parking_slot)
VALUES 
('Akash', 'akash@example.com', '9876543210', 'A-101', '1234', 'KA-01-AB-1234', 'A-01');

INSERT INTO "A-partment".workers (full_name, email, phone, worker_type, password)
VALUES 
('Guard Ramesh', 'worker@example.com', '9123456780', 'Security', '1234');

INSERT INTO "A-partment".parking (slot_number, parking_type, status, resident_id)
VALUES 
('A-01', 'RESIDENT', 'OCCUPIED', 1),
('V-01', 'VISITOR', 'AVAILABLE', NULL),
('V-02', 'VISITOR', 'AVAILABLE', NULL),
('V-08', 'VISITOR', 'AVAILABLE', NULL);

INSERT INTO "A-partment".payments (resident_id, month_year, amount, due_date, status)
VALUES 
(1, 'September 2026', 2500.00, '2026-09-30', 'PENDING');
