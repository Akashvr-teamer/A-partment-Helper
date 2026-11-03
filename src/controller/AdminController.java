package controller;

import database.DBConnection;
import model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AdminController {

    // 1. Overview
    public SocietyMetrics getSocietyMetrics() throws SQLException {
        SocietyMetrics m = new SocietyMetrics();
        LocalDate today = LocalDate.now();

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return m;

            // 1. Total residents count
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM residents")) {
                if (rs.next()) m.setTotalResidents(rs.getInt(1));
            }

            // 2. Total workers count
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM workers")) {
                if (rs.next()) m.setTotalWorkers(rs.getInt(1));
            }

            // 3. Open complaints count
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM complaints WHERE status != 'COMPLETED'")) {
                if (rs.next()) m.setOpenComplaints(rs.getInt(1));
            }

            // 4. Unpaid dues sum
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE status != 'PAID'")) {
                if (rs.next()) m.setTotalUnpaidDues(rs.getDouble(1));
            }

            // 5. Complaints detailed breakdown
            String compBreakdown =
                "SELECT " +
                "  COUNT(*) AS total_c, " +
                "  COUNT(CASE WHEN status IN ('PENDING', 'ASSIGNED') THEN 1 END) AS pending_c, " +
                "  COUNT(CASE WHEN status IN ('IN_PROGRESS', 'IN PROGRESS') THEN 1 END) AS inprog_c, " +
                "  COUNT(CASE WHEN status IN ('COMPLETED', 'RESOLVED') THEN 1 END) AS comp_c " +
                "FROM complaints";
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(compBreakdown)) {
                if (rs.next()) {
                    m.setTotalComplaints(rs.getInt("total_c"));
                    m.setPendingComplaints(rs.getInt("pending_c"));
                    m.setInProgressComplaints(rs.getInt("inprog_c"));
                    m.setCompletedComplaints(rs.getInt("comp_c"));
                }
            }

            // 6. Billing detailed breakdown
            String payBreakdown = "SELECT amount, due_date, status FROM payments";
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(payBreakdown)) {
                double billed = 0, paid = 0, pending = 0, overdue = 0;
                while (rs.next()) {
                    double amt = rs.getDouble("amount");
                    Date due = rs.getDate("due_date");
                    String status = rs.getString("status");
                    billed += amt;
                    if ("PAID".equalsIgnoreCase(status)) {
                        paid += amt;
                    } else {
                        pending += amt;
                    }
                    if (due != null && today.isAfter(due.toLocalDate())) {
                        overdue += amt;
                    }
                }
                m.setTotalBilled(billed);
                m.setTotalPaid(paid);
                m.setTotalPending(pending);
                m.setTotalOverdue(overdue);
            }
        }

        return m;
    }

    public List<Complaint> getRecentComplaints(int limit) throws SQLException {
        List<Complaint> list = new ArrayList<>();
        String sql =
            "SELECT c.complaint_id, c.resident_id, r.full_name AS resident_name, r.apartment_no, " +
            "       c.title, c.category, c.worker_id, COALESCE(w.full_name, 'Unassigned') AS worker_name, " +
            "       c.status, c.created_at, c.completed_at, c.description " +
            "FROM complaints c " +
            "JOIN residents r ON c.resident_id = r.resident_id " +
            "LEFT JOIN workers w ON c.worker_id = w.worker_id " +
            "ORDER BY c.complaint_id DESC LIMIT " + Math.max(1, limit);

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapComplaintRow(rs));
            }
        }
        return list;
    }

    public List<Payment> getRecentPayments(int limit) throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql =
            "SELECT p.payment_id, p.resident_id, r.full_name AS resident_name, r.apartment_no, " +
            "       p.month_year, p.amount, p.due_date, p.payment_date, p.status, p.payment_method, " +
            "       p.payment_type, p.complaint_id " +
            "FROM payments p " +
            "JOIN residents r ON p.resident_id = r.resident_id " +
            "ORDER BY p.payment_id DESC LIMIT " + Math.max(1, limit);

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapPaymentRow(rs));
            }
        }
        return list;
    }

    // 2. Complaints Management
    public List<Complaint> getAllComplaints() throws SQLException {
        List<Complaint> list = new ArrayList<>();
        String sql =
            "SELECT c.complaint_id, c.resident_id, r.full_name AS resident_name, r.apartment_no, " +
            "       c.title, c.category, c.worker_id, COALESCE(w.full_name, 'Unassigned') AS worker_name, " +
            "       c.status, c.created_at, c.completed_at, c.description " +
            "FROM complaints c " +
            "JOIN residents r ON c.resident_id = r.resident_id " +
            "LEFT JOIN workers w ON c.worker_id = w.worker_id " +
            "ORDER BY c.complaint_id DESC";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapComplaintRow(rs));
            }
        }
        return list;
    }

    public String getComplaintDescription(int complaintId) throws SQLException {
        String sql = "SELECT description FROM complaints WHERE complaint_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("description");
                }
            }
        }
        return null;
    }

    public boolean assignComplaint(int complaintId, int workerId) throws SQLException {
        String sql = "UPDATE complaints SET worker_id = ?, status = 'ASSIGNED' WHERE complaint_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, workerId);
            ps.setInt(2, complaintId);
            return ps.executeUpdate() > 0;
        }
    }

    public Integer autoAssignComplaint(int complaintId, String category) throws SQLException {
        Integer bestWorkerId = findBestWorkerForCategory(category);
        if (bestWorkerId != null) {
            assignComplaint(complaintId, bestWorkerId);
        }
        return bestWorkerId;
    }

    // 3. Billing Management
    public List<Payment> getAllPayments() throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql =
            "SELECT p.payment_id, p.resident_id, r.full_name AS resident_name, r.apartment_no, " +
            "       p.month_year, p.amount, p.due_date, p.payment_date, p.status, p.payment_method, " +
            "       p.payment_type, p.complaint_id " +
            "FROM payments p " +
            "JOIN residents r ON p.resident_id = r.resident_id " +
            "ORDER BY p.payment_id DESC";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapPaymentRow(rs));
            }
        }
        return list;
    }

    public boolean generateCustomBill(int residentId, String title, double amount, Date dueDate) throws SQLException {
        if (title != null && title.length() > 255) {
            title = title.substring(0, 255);
        }
        String sql = "INSERT INTO payments (resident_id, month_year, amount, due_date, status, payment_type) " +
                     "VALUES (?, ?, ?, ?::DATE, 'PENDING', 'CUSTOM')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, residentId);
            ps.setString(2, title);
            ps.setDouble(3, amount);
            ps.setDate(4, dueDate);
            return ps.executeUpdate() > 0;
        }
    }

    // 4. Registration
    public boolean isEmailTaken(String email) throws SQLException {
        String checkSql = "SELECT 'resident' FROM residents WHERE lower(email) = lower(?) " +
                          "UNION SELECT 'worker' FROM workers WHERE lower(email) = lower(?) " +
                          "UNION SELECT 'admin' FROM admins WHERE lower(email) = lower(?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(checkSql)) {
            ps.setString(1, email.trim());
            ps.setString(2, email.trim());
            ps.setString(3, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int registerResident(Resident resident) throws SQLException {
        if (isEmailTaken(resident.getEmail())) {
            throw new IllegalArgumentException("The email '" + resident.getEmail() + "' is already registered in the system.");
        }

        String sql = "INSERT INTO residents (full_name, email, phone, apartment_no, password, vehicle_no, parking_slot) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        int genId = 0;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, resident.getFullName());
            ps.setString(2, resident.getEmail().trim().toLowerCase());
            ps.setString(3, resident.getPhone());
            ps.setString(4, resident.getApartmentNo());
            ps.setString(5, resident.getPassword());
            ps.setString(6, resident.getVehicleNo() != null && !resident.getVehicleNo().isBlank() ? resident.getVehicleNo().trim() : null);
            ps.setString(7, resident.getParkingSlot() != null && !resident.getParkingSlot().isBlank() ? resident.getParkingSlot().trim() : null);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        genId = gk.getInt(1);
                        resident.setResidentId(genId);
                    }
                }
            }
        }

        if (genId > 0 && resident.getParkingSlot() != null && !resident.getParkingSlot().isBlank()) {
            syncResidentParking(resident.getParkingSlot(), genId);
        }
        return genId;
    }

    public int registerWorker(Worker worker) throws SQLException {
        if (isEmailTaken(worker.getEmail())) {
            throw new IllegalArgumentException("The email '" + worker.getEmail() + "' is already registered in the system.");
        }

        String sql = "INSERT INTO workers (full_name, email, phone, worker_type, password) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, worker.getFullName());
            ps.setString(2, worker.getEmail().trim().toLowerCase());
            ps.setString(3, worker.getPhone());
            ps.setString(4, worker.getWorkerType());
            ps.setString(5, worker.getPassword());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        int genId = gk.getInt(1);
                        worker.setWorkerId(genId);
                        return genId;
                    }
                }
            }
        }
        return 0;
    }

    // 5. Directory Management
    public List<Resident> getAllResidents() throws SQLException {
        List<Resident> list = new ArrayList<>();
        String sql = "SELECT * FROM residents ORDER BY apartment_no ASC, resident_id ASC";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResidentRow(rs));
            }
        }
        return list;
    }

    public Resident getResidentById(int residentId) throws SQLException {
        String sql = "SELECT * FROM residents WHERE resident_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, residentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResidentRow(rs);
                }
            }
        }
        return null;
    }

    public boolean updateResident(Resident resident, boolean updatePassword) throws SQLException {
        String sql = updatePassword
                ? "UPDATE residents SET full_name = ?, phone = ?, apartment_no = ?, vehicle_no = ?, parking_slot = ?, password = ? WHERE resident_id = ?"
                : "UPDATE residents SET full_name = ?, phone = ?, apartment_no = ?, vehicle_no = ?, parking_slot = ? WHERE resident_id = ?";
        boolean ok;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, resident.getFullName());
            ps.setString(2, resident.getPhone());
            ps.setString(3, resident.getApartmentNo());
            ps.setString(4, resident.getVehicleNo() != null && !resident.getVehicleNo().isBlank() ? resident.getVehicleNo().trim() : null);
            ps.setString(5, resident.getParkingSlot() != null && !resident.getParkingSlot().isBlank() ? resident.getParkingSlot().trim() : null);
            if (updatePassword) {
                ps.setString(6, resident.getPassword());
                ps.setInt(7, resident.getResidentId());
            } else {
                ps.setInt(6, resident.getResidentId());
            }
            ok = ps.executeUpdate() > 0;
        }

        if (ok && resident.getParkingSlot() != null && !resident.getParkingSlot().isBlank()) {
            syncResidentParking(resident.getParkingSlot(), resident.getResidentId());
        }
        return ok;
    }

    public boolean deleteResident(int residentId) throws SQLException {
        String sql = "DELETE FROM residents WHERE resident_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, residentId);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Worker> getAllWorkers() throws SQLException {
        List<Worker> list = new ArrayList<>();
        String sql = "SELECT * FROM workers ORDER BY full_name ASC";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapWorkerRow(rs));
            }
        }
        return list;
    }

    public List<Worker> getAllWorkersWithWorkload() throws SQLException {
        List<Worker> list = new ArrayList<>();
        String sql =
            "SELECT w.worker_id, w.full_name, w.worker_type, w.email, w.phone, w.password, w.created_at, " +
            "       COUNT(CASE WHEN c.status IN ('ASSIGNED', 'IN_PROGRESS', 'IN PROGRESS') THEN 1 END) AS active_cnt, " +
            "       COUNT(CASE WHEN c.status IN ('COMPLETED', 'RESOLVED') THEN 1 END) AS resolved_cnt " +
            "FROM workers w " +
            "LEFT JOIN complaints c ON w.worker_id = c.worker_id " +
            "GROUP BY w.worker_id, w.full_name, w.worker_type, w.email, w.phone, w.password, w.created_at " +
            "ORDER BY w.worker_id ASC";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Worker w = mapWorkerRow(rs);
                w.setActiveTasks(rs.getInt("active_cnt"));
                w.setCompletedTasks(rs.getInt("resolved_cnt"));
                list.add(w);
            }
        }
        return list;
    }

    public List<String> getDistinctWorkerTypes() throws SQLException {
        List<String> types = new ArrayList<>();
        String sql = "SELECT DISTINCT worker_type FROM workers WHERE worker_type IS NOT NULL AND worker_type != '' ORDER BY worker_type";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                types.add(rs.getString(1));
            }
        }
        return types;
    }

    public Worker getWorkerById(int workerId) throws SQLException {
        String sql = "SELECT * FROM workers WHERE worker_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, workerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapWorkerRow(rs);
                }
            }
        }
        return null;
    }

    public boolean updateWorker(Worker worker, boolean updatePassword) throws SQLException {
        String sql = updatePassword
                ? "UPDATE workers SET full_name = ?, phone = ?, worker_type = ?, password = ? WHERE worker_id = ?"
                : "UPDATE workers SET full_name = ?, phone = ?, worker_type = ? WHERE worker_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, worker.getFullName());
            ps.setString(2, worker.getPhone());
            ps.setString(3, worker.getWorkerType());
            if (updatePassword) {
                ps.setString(4, worker.getPassword());
                ps.setInt(5, worker.getWorkerId());
            } else {
                ps.setInt(4, worker.getWorkerId());
            }
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteWorker(int workerId) throws SQLException {
        // Reset any complaints currently assigned to this worker so they are not orphaned
        String resetSql = "UPDATE complaints SET worker_id = NULL, status = 'PENDING' " +
                          "WHERE worker_id = ? AND status IN ('ASSIGNED', 'IN_PROGRESS', 'IN PROGRESS')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(resetSql)) {
            ps.setInt(1, workerId);
            ps.executeUpdate();
        }

        String sql = "DELETE FROM workers WHERE worker_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, workerId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean syncResidentParking(String slotNumber, int residentId) throws SQLException {
        if (slotNumber == null || slotNumber.isBlank()) return false;

        String sql = "INSERT INTO parking (slot_number, parking_type, status, resident_id) " +
                     "VALUES (?, 'RESIDENT', 'OCCUPIED', ?) " +
                     "ON CONFLICT (slot_number) DO UPDATE SET resident_id = EXCLUDED.resident_id, status = 'OCCUPIED'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, slotNumber.trim());
            ps.setInt(2, residentId);
            return ps.executeUpdate() > 0;
        }
    }

    public Integer findBestWorkerForCategory(String category) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return null;

            // Step 1: Look for an unassigned worker (0 active complaints in PENDING, ASSIGNED, IN_PROGRESS)
            String unassignedSql =
                "SELECT w.worker_id " +
                "FROM workers w " +
                "WHERE NOT EXISTS (" +
                "    SELECT 1 FROM complaints c " +
                "    WHERE c.worker_id = w.worker_id " +
                "    AND c.status IN ('PENDING', 'ASSIGNED', 'IN_PROGRESS', 'IN PROGRESS')" +
                ") " +
                "ORDER BY CASE WHEN lower(w.worker_type) = lower(?) THEN 0 ELSE 1 END, w.worker_id ASC " +
                "LIMIT 1";

            try (PreparedStatement ps = con.prepareStatement(unassignedSql)) {
                ps.setString(1, category != null ? category : "");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("worker_id");
                    }
                }
            }

            // Step 2: ONLY IF NO workers are unassigned, look among already-assigned workers (least busy)
            String busySql =
                "SELECT w.worker_id, COUNT(c.complaint_id) AS active_cnt " +
                "FROM workers w " +
                "LEFT JOIN complaints c ON w.worker_id = c.worker_id " +
                "    AND c.status IN ('PENDING', 'ASSIGNED', 'IN_PROGRESS', 'IN PROGRESS') " +
                "GROUP BY w.worker_id, w.worker_type " +
                "ORDER BY active_cnt ASC, CASE WHEN lower(w.worker_type) = lower(?) THEN 0 ELSE 1 END, w.worker_id ASC " +
                "LIMIT 1";

            try (PreparedStatement ps = con.prepareStatement(busySql)) {
                ps.setString(1, category != null ? category : "");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("worker_id");
                    }
                }
            }
        }
        return null;
    }

    private Resident mapResidentRow(ResultSet rs) throws SQLException {
        Resident r = new Resident();
        r.setResidentId(rs.getInt("resident_id"));
        r.setFullName(rs.getString("full_name"));
        r.setEmail(rs.getString("email"));
        r.setPhone(rs.getString("phone"));
        r.setApartmentNo(rs.getString("apartment_no"));
        r.setPassword(rs.getString("password"));
        r.setVehicleNo(rs.getString("vehicle_no"));
        r.setParkingSlot(rs.getString("parking_slot"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        return r;
    }

    private Worker mapWorkerRow(ResultSet rs) throws SQLException {
        Worker w = new Worker();
        w.setWorkerId(rs.getInt("worker_id"));
        w.setFullName(rs.getString("full_name"));
        w.setEmail(rs.getString("email"));
        w.setPhone(rs.getString("phone"));
        w.setWorkerType(rs.getString("worker_type"));
        w.setPassword(rs.getString("password"));
        w.setCreatedAt(rs.getTimestamp("created_at"));
        return w;
    }

    private Complaint mapComplaintRow(ResultSet rs) throws SQLException {
        Complaint c = new Complaint();
        c.setComplaintId(rs.getInt("complaint_id"));
        c.setResidentId(rs.getInt("resident_id"));
        int wId = rs.getInt("worker_id");
        c.setWorkerId(rs.wasNull() ? null : wId);
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setCategory(rs.getString("category"));
        c.setStatus(rs.getString("status"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        c.setCompletedAt(rs.getTimestamp("completed_at"));

        try {
            c.setResidentName(rs.getString("resident_name"));
            c.setApartmentNo(rs.getString("apartment_no"));
            c.setWorkerName(rs.getString("worker_name"));
        } catch (SQLException ignored) {}

        return c;
    }

    private Payment mapPaymentRow(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setPaymentId(rs.getInt("payment_id"));
        p.setResidentId(rs.getInt("resident_id"));
        p.setMonthYear(rs.getString("month_year"));
        p.setAmount(rs.getDouble("amount"));
        p.setDueDate(rs.getDate("due_date"));
        p.setPaymentDate(rs.getDate("payment_date"));
        p.setStatus(rs.getString("status"));
        p.setPaymentMethod(rs.getString("payment_method"));
        p.setPaymentType(rs.getString("payment_type"));
        int cId = rs.getInt("complaint_id");
        p.setComplaintId(rs.wasNull() ? null : cId);

        try {
            p.setResidentName(rs.getString("resident_name"));
            p.setApartmentNo(rs.getString("apartment_no"));
        } catch (SQLException ignored) {}

        return p;
    }
}
