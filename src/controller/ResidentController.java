package controller;

import database.DBConnection;
import model.*;
import util.CodeGenerator;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ResidentController {

    public Resident getProfile(int residentId) throws SQLException {
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

    public ResidentMetrics getMetrics(int residentId) throws SQLException {
        ResidentMetrics m = new ResidentMetrics();
        LocalDate today = LocalDate.now();

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return m;

            // Unpaid dues & Overdue breakdown
            String paySql = "SELECT amount, due_date, status FROM payments WHERE resident_id = ?";
            try (PreparedStatement ps = con.prepareStatement(paySql)) {
                ps.setInt(1, residentId);
                try (ResultSet rs = ps.executeQuery()) {
                    double pending = 0;
                    double overdue = 0;
                    int paid = 0;
                    while (rs.next()) {
                        double amt = rs.getDouble("amount");
                        Date due = rs.getDate("due_date");
                        String status = rs.getString("status");
                        if ("PAID".equalsIgnoreCase(status)) {
                            paid++;
                        } else {
                            pending += amt;
                            if (due != null && today.isAfter(due.toLocalDate())) {
                                overdue += amt;
                            }
                        }
                    }
                    m.setTotalDues(pending);
                    m.setOverdueDues(overdue);
                    m.setPaidCount(paid);
                }
            }

            // Open complaints count
            String compSql = "SELECT COUNT(*) FROM complaints WHERE resident_id = ? AND status != 'COMPLETED'";
            try (PreparedStatement ps = con.prepareStatement(compSql)) {
                ps.setInt(1, residentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) m.setOpenComplaintsCount(rs.getInt(1));
                }
            }

            // Active visitors count
            String visSql = "SELECT COUNT(*) FROM visitors WHERE resident_id = ? AND status IN ('PENDING', 'ENTERED')";
            try (PreparedStatement ps = con.prepareStatement(visSql)) {
                ps.setInt(1, residentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) m.setActiveVisitorsCount(rs.getInt(1));
                }
            }
        }

        return m;
    }

    public List<Visitor> getRecentVisitors(int residentId) throws SQLException {
        List<Visitor> list = new ArrayList<>();
        String sql = "SELECT * FROM visitors WHERE resident_id = ? ORDER BY visitor_id DESC LIMIT 15";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, residentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapVisitorRow(rs));
                }
            }
        }
        return list;
    }

    public String createVisitorPass(int residentId, String name, String phone,
                                    boolean hasVehicle, String vehicleNo,
                                    Timestamp validFrom, Timestamp validUntil) throws SQLException {
        String passCode = CodeGenerator.generateCode();
        String passHash = CodeGenerator.sha256(passCode);

        String sql = "INSERT INTO visitors(resident_id, visitor_name, phone, has_vehicle, vehicle_no, access_hash, valid_from, valid_until, status) " +
                     "VALUES(?, ?, ?, ?, ?, ?, ?, ?, 'PENDING')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, residentId);
            ps.setString(2, name);
            ps.setString(3, phone);
            ps.setBoolean(4, hasVehicle);
            ps.setString(5, hasVehicle ? vehicleNo : null);
            ps.setString(6, passHash);
            ps.setTimestamp(7, validFrom);
            ps.setTimestamp(8, validUntil);
            ps.executeUpdate();
        }

        return passCode;
    }

    public List<Complaint> getComplaints(int residentId) throws SQLException {
        List<Complaint> list = new ArrayList<>();
        String sql =
            "SELECT c.complaint_id, c.resident_id, r.full_name AS resident_name, r.apartment_no, " +
            "       c.title, c.category, c.worker_id, COALESCE(w.full_name, 'Unassigned') AS worker_name, " +
            "       c.status, c.created_at, c.completed_at, c.description " +
            "FROM complaints c " +
            "JOIN residents r ON c.resident_id = r.resident_id " +
            "LEFT JOIN workers w ON c.worker_id = w.worker_id " +
            "WHERE c.resident_id = ? " +
            "ORDER BY c.complaint_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, residentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapComplaintRow(rs));
                }
            }
        }
        return list;
    }

    public boolean fileComplaint(int residentId, String title, String category, String description) throws SQLException {
        Integer workerId = findBestWorkerForCategory(category);
        String status = (workerId != null && workerId > 0) ? "ASSIGNED" : "PENDING";
        String sql = "INSERT INTO complaints (resident_id, worker_id, title, description, category, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, residentId);
            if (workerId != null && workerId > 0) {
                ps.setInt(2, workerId);
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, title);
            ps.setString(4, description);
            ps.setString(5, category);
            ps.setString(6, status);

            return ps.executeUpdate() > 0;
        }
    }

    public List<Payment> getPayments(int residentId) throws SQLException {
        List<Payment> list = new ArrayList<>();
        String sql =
            "SELECT p.payment_id, p.resident_id, r.full_name AS resident_name, r.apartment_no, " +
            "       p.month_year, p.amount, p.due_date, p.payment_date, p.status, p.payment_method, " +
            "       p.payment_type, p.complaint_id " +
            "FROM payments p " +
            "JOIN residents r ON p.resident_id = r.resident_id " +
            "WHERE p.resident_id = ? " +
            "ORDER BY p.payment_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, residentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPaymentRow(rs));
                }
            }
        }
        return list;
    }

    public boolean payBill(int paymentId, String paymentMethod) throws SQLException {
        return markAsPaid(paymentId, paymentMethod);
    }

    public boolean markAsPaid(int paymentId, String paymentMethod) throws SQLException {
        String sql = "UPDATE payments SET status = 'PAID', payment_date = CURRENT_DATE, payment_method = ? " +
                     "WHERE payment_id = ? AND status != 'PAID'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, paymentMethod != null ? paymentMethod : "ONLINE");
            ps.setInt(2, paymentId);
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

    private Visitor mapVisitorRow(ResultSet rs) throws SQLException {
        Visitor v = new Visitor();
        v.setVisitorId(rs.getInt("visitor_id"));
        v.setResidentId(rs.getInt("resident_id"));
        v.setVisitorName(rs.getString("visitor_name"));
        v.setPhone(rs.getString("phone"));
        v.setHasVehicle(rs.getBoolean("has_vehicle"));
        v.setVehicleNo(rs.getString("vehicle_no"));
        v.setAccessHash(rs.getString("access_hash"));
        v.setExitCode(rs.getString("exit_code"));
        v.setValidFrom(rs.getTimestamp("valid_from"));
        v.setValidUntil(rs.getTimestamp("valid_until"));
        v.setEntryTime(rs.getTimestamp("entry_time"));
        v.setExitTime(rs.getTimestamp("exit_time"));
        v.setStatus(rs.getString("status"));
        v.setParkingSlot(rs.getString("parking_slot"));
        v.setCreatedAt(rs.getTimestamp("created_at"));
        return v;
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
