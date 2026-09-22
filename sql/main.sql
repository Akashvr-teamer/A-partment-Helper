-- ============================================
-- SMART APARTMENT SOCIETY MANAGEMENT SYSTEM
-- ============================================

DROP DATABASE IF EXISTS apartment_society;

CREATE DATABASE apartment_society;

USE apartment_society;


-- ============================================
-- 1. RESIDENTS
-- ============================================

CREATE TABLE residents (
    resident_id INT PRIMARY KEY AUTO_INCREMENT,
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

CREATE TABLE workers (
    worker_id INT PRIMARY KEY AUTO_INCREMENT,
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

CREATE TABLE parking (
    parking_id INT PRIMARY KEY AUTO_INCREMENT,
    slot_number VARCHAR(10) UNIQUE NOT NULL,
    parking_type ENUM('RESIDENT', 'VISITOR') NOT NULL,
    status ENUM('AVAILABLE', 'OCCUPIED') DEFAULT 'AVAILABLE',

    resident_id INT NULL,
    visitor_id INT NULL,

    FOREIGN KEY (resident_id)
        REFERENCES residents(resident_id)
        ON DELETE SET NULL
);


-- ============================================
-- 4. VISITORS
-- ============================================

CREATE TABLE visitors (
    visitor_id INT PRIMARY KEY AUTO_INCREMENT,

    resident_id INT NOT NULL,

    visitor_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) NOT NULL,

    has_vehicle BOOLEAN DEFAULT FALSE,
    vehicle_no VARCHAR(20),

    access_hash VARCHAR(64) NOT NULL,

    valid_from DATETIME NOT NULL,
    valid_until DATETIME NOT NULL,

    entry_time DATETIME NULL,
    exit_time DATETIME NULL,

    status ENUM(
        'PENDING',
        'ENTERED',
        'EXITED',
        'EXPIRED'
    ) DEFAULT 'PENDING',

    parking_slot VARCHAR(10) NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (resident_id)
        REFERENCES residents(resident_id)
        ON DELETE CASCADE
);


-- ============================================
-- 5. COMPLAINTS / MAINTENANCE
-- ============================================

CREATE TABLE complaints (
    complaint_id INT PRIMARY KEY AUTO_INCREMENT,

    resident_id INT NOT NULL,
    worker_id INT NULL,

    title VARCHAR(150) NOT NULL,
    description TEXT,

    category VARCHAR(50),

    status ENUM(
        'PENDING',
        'ASSIGNED',
        'IN_PROGRESS',
        'COMPLETED'
    ) DEFAULT 'PENDING',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME NULL,

    FOREIGN KEY (resident_id)
        REFERENCES residents(resident_id)
        ON DELETE CASCADE,

    FOREIGN KEY (worker_id)
        REFERENCES workers(worker_id)
        ON DELETE SET NULL
);


-- ============================================
-- 6. PAYMENTS
-- ============================================

CREATE TABLE payments (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,

    resident_id INT NOT NULL,

    month_year VARCHAR(20) NOT NULL,

    amount DECIMAL(10,2) NOT NULL,

    due_date DATE NOT NULL,

    payment_date DATE NULL,

    status ENUM(
        'PENDING',
        'PAID',
        'OVERDUE'
    ) DEFAULT 'PENDING',

    payment_method VARCHAR(30),

    transaction_id VARCHAR(100),

    FOREIGN KEY (resident_id)
        REFERENCES residents(resident_id)
        ON DELETE CASCADE
);