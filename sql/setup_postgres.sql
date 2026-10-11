-- A-partment Helper: PostgreSQL / Supabase setup (public schema)
-- Run this script in the Supabase SQL Editor.

CREATE TABLE IF NOT EXISTS residents (
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

CREATE TABLE IF NOT EXISTS workers (
  worker_id SERIAL PRIMARY KEY,
  full_name VARCHAR(100) NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  phone VARCHAR(15),
  worker_type VARCHAR(50),
  password VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS admins (
  admin_id SERIAL PRIMARY KEY,
  full_name VARCHAR(100) NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  phone VARCHAR(15),
  password VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS visitors (
  visitor_id SERIAL PRIMARY KEY,
  resident_id INT NOT NULL REFERENCES residents(resident_id) ON DELETE CASCADE,
  visitor_name VARCHAR(100) NOT NULL,
  phone VARCHAR(15) NOT NULL,
  has_vehicle BOOLEAN DEFAULT FALSE,
  vehicle_no VARCHAR(20),
  access_hash VARCHAR(64) NOT NULL,
  exit_code VARCHAR(10),
  valid_from TIMESTAMP NOT NULL,
  valid_until TIMESTAMP NOT NULL,
  entry_time TIMESTAMP,
  exit_time TIMESTAMP,
  status VARCHAR(20) DEFAULT 'PENDING' CHECK(status IN ('PENDING','ENTERED','EXITED','EXPIRED')),
  parking_slot VARCHAR(15),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CHECK(valid_until > valid_from)
);

CREATE TABLE IF NOT EXISTS parking (
  parking_id SERIAL PRIMARY KEY,
  slot_number VARCHAR(10) UNIQUE NOT NULL,
  parking_type VARCHAR(20) NOT NULL CHECK(parking_type IN ('RESIDENT','VISITOR')),
  status VARCHAR(20) DEFAULT 'AVAILABLE' CHECK(status IN ('AVAILABLE','OCCUPIED')),
  resident_id INT REFERENCES residents(resident_id) ON DELETE SET NULL,
  visitor_id INT REFERENCES visitors(visitor_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS complaints (
  complaint_id SERIAL PRIMARY KEY,
  resident_id INT NOT NULL REFERENCES residents(resident_id) ON DELETE CASCADE,
  worker_id INT REFERENCES workers(worker_id) ON DELETE SET NULL,
  title VARCHAR(150) NOT NULL,
  description TEXT,
  category VARCHAR(50),
  status VARCHAR(20) DEFAULT 'PENDING' CHECK(status IN ('PENDING','ASSIGNED','IN_PROGRESS','COMPLETED')),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  completed_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payments (
  payment_id SERIAL PRIMARY KEY,
  resident_id INT NOT NULL REFERENCES residents(resident_id) ON DELETE CASCADE,
  month_year VARCHAR(255) NOT NULL,
  amount DECIMAL(10,2) NOT NULL CHECK(amount >= 0),
  due_date DATE NOT NULL,
  payment_date DATE,
  status VARCHAR(20) DEFAULT 'PENDING' CHECK(status IN ('PENDING','PAID','OVERDUE')),
  payment_method VARCHAR(30),
  transaction_id VARCHAR(100),
  payment_type VARCHAR(50) DEFAULT 'MAINTENANCE',
  complaint_id INT REFERENCES complaints(complaint_id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_visitors_resident ON visitors(resident_id, visitor_id DESC);
CREATE INDEX IF NOT EXISTS idx_complaints_worker_status ON complaints(worker_id, status);
CREATE INDEX IF NOT EXISTS idx_payments_resident_status ON payments(resident_id, status);

-- Sample Demo Records
INSERT INTO residents(full_name, email, phone, apartment_no, password, vehicle_no, parking_slot)
VALUES('Akash', 'akash@example.com', '9876543210', 'A-101', '1234', 'KA-01-AB-1234', 'A-01')
ON CONFLICT(email) DO NOTHING;

INSERT INTO workers(full_name, email, phone, worker_type, password)
VALUES('Guard Ramesh', 'worker@example.com', '9123456780', 'Security', '1234')
ON CONFLICT(email) DO NOTHING;

INSERT INTO admins(full_name, email, phone, password)
VALUES('Society Admin', 'admin@apartment.com', '9876500000', 'Admin@2026'),
      ('Society Admin', 'admin@example.com', '9876500001', '1234')
ON CONFLICT(email) DO NOTHING;

INSERT INTO parking(slot_number, parking_type, status)
VALUES('V-01', 'VISITOR', 'AVAILABLE'),
      ('V-02', 'VISITOR', 'AVAILABLE'),
      ('V-03', 'VISITOR', 'AVAILABLE'),
      ('V-04', 'VISITOR', 'AVAILABLE'),
      ('V-05', 'VISITOR', 'AVAILABLE'),
      ('V-06', 'VISITOR', 'AVAILABLE')
ON CONFLICT(slot_number) DO NOTHING;

INSERT INTO payments(resident_id, month_year, amount, due_date, status)
SELECT resident_id, 'October 2026', 2500.00, DATE '2026-10-31', 'PENDING'
FROM residents WHERE email = 'akash@example.com'
ON CONFLICT(resident_id, month_year) DO NOTHING;
