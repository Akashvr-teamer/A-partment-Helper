-- ============================================
-- SMART APARTMENT SOCIETY MANAGEMENT SYSTEM
-- PostgreSQL Populate Script for "A-partment" Schema
-- ============================================

SET search_path TO "A-partment";

-- Clear existing data and restart identity sequences so IDs start from 1
TRUNCATE TABLE 
    "A-partment".payments,
    "A-partment".complaints,
    "A-partment".visitors,
    "A-partment".parking,
    "A-partment".workers,
    "A-partment".residents 
RESTART IDENTITY CASCADE;

-- ============================================
-- RESIDENTS
-- ============================================

INSERT INTO "A-partment".residents
(full_name, email, phone, apartment_no, password, vehicle_no, parking_slot)
VALUES
('Akash Kumar', 'akash@gmail.com', '9876543210', 'A-101', '1234', 'KL07AB1234', 'A-01'),
('Rahul Menon', 'rahul@gmail.com', '9876543211', 'A-102', '1234', 'KL08CD5678', 'A-02'),
('Meera Nair', 'meera@gmail.com', '9876543212', 'B-201', '1234', NULL, NULL);

-- ============================================
-- WORKERS
-- ============================================

INSERT INTO "A-partment".workers
(full_name, email, phone, worker_type, password)
VALUES
('Suresh Kumar', 'suresh@apartment.com', '9876500001', 'Electrician', '1234'),
('Ramesh Das', 'ramesh@apartment.com', '9876500002', 'Plumber', '1234'),
('Anil Raj', 'anil@apartment.com', '9876500003', 'Maintenance', '1234');

-- ============================================
-- PARKING SLOTS
-- ============================================

INSERT INTO "A-partment".parking
(slot_number, parking_type, status, resident_id)
VALUES
('A-01', 'RESIDENT', 'OCCUPIED', 1),
('A-02', 'RESIDENT', 'OCCUPIED', 2),
('A-03', 'RESIDENT', 'AVAILABLE', NULL),
('A-04', 'RESIDENT', 'AVAILABLE', NULL),
('A-05', 'RESIDENT', 'AVAILABLE', NULL),
('V-01', 'VISITOR', 'AVAILABLE', NULL),
('V-02', 'VISITOR', 'AVAILABLE', NULL),
('V-03', 'VISITOR', 'AVAILABLE', NULL),
('V-04', 'VISITOR', 'AVAILABLE', NULL),
('V-05', 'VISITOR', 'AVAILABLE', NULL);

-- ============================================
-- VISITORS
-- ============================================

INSERT INTO "A-partment".visitors
(
    resident_id,
    visitor_name,
    phone,
    has_vehicle,
    vehicle_no,
    access_hash,
    valid_from,
    valid_until,
    status,
    parking_slot
)
VALUES
(
    1,
    'Sarah Thomas',
    '9876543001',
    TRUE,
    'KL07XY1234',
    'demo_hash_001',
    '2026-09-22 10:00:00',
    '2026-09-22 18:00:00',
    'PENDING',
    NULL
),
(
    1,
    'Arjun Nair',
    '9876543002',
    TRUE,
    'KL08AB4567',
    'demo_hash_002',
    '2026-09-22 09:00:00',
    '2026-09-22 17:00:00',
    'ENTERED',
    'V-01'
),
(
    2,
    'Meera Sharma',
    '9876543003',
    FALSE,
    NULL,
    'demo_hash_003',
    '2026-09-22 12:00:00',
    '2026-09-22 16:00:00',
    'PENDING',
    NULL
);

-- ============================================
-- COMPLAINTS
-- ============================================

INSERT INTO "A-partment".complaints
(
    resident_id,
    worker_id,
    title,
    description,
    category,
    status
)
VALUES
(
    1,
    1,
    'Broken Corridor Light',
    'The light near apartment A-101 is not working.',
    'Electrical',
    'IN_PROGRESS'
),
(
    2,
    2,
    'Water Leakage',
    'There is water leakage near the kitchen sink.',
    'Plumbing',
    'ASSIGNED'
),
(
    3,
    NULL,
    'Lift Problem',
    'The lift is making unusual noises.',
    'Lift',
    'PENDING'
);

-- ============================================
-- PAYMENTS
-- ============================================

INSERT INTO "A-partment".payments
(
    resident_id,
    month_year,
    amount,
    due_date,
    payment_date,
    status,
    payment_method,
    transaction_id
)
VALUES
(
    1,
    'September 2026',
    2500.00,
    '2026-09-10',
    '2026-09-08',
    'PAID',
    'UPI',
    'UPI20260908001'
),
(
    2,
    'September 2026',
    2500.00,
    '2026-09-10',
    NULL,
    'PENDING',
    NULL,
    NULL
),
(
    3,
    'September 2026',
    2500.00,
    '2026-09-10',
    NULL,
    'OVERDUE',
    NULL,
    NULL
);
