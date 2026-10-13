package model.dao;

import database.DBConnection;
import model.Admin;
import model.SocietyMetrics;

import java.sql.*;
import java.time.LocalDate;

public class AdminDAO {

    public Admin authenticate(String email, String password) throws SQLException {
        String sql = "SELECT * FROM admins WHERE lower(email) = lower(?) AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Admin getById(int adminId) throws SQLException {
        String sql = "SELECT * FROM admins WHERE admin_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

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
                        if (due != null && today.isAfter(due.toLocalDate())) {
                            overdue += amt;
                        }
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

    private Admin mapRow(ResultSet rs) throws SQLException {
        Admin a = new Admin();
        a.setAdminId(rs.getInt("admin_id"));
        a.setFullName(rs.getString("full_name"));
        a.setEmail(rs.getString("email"));
        a.setPhone(rs.getString("phone"));
        a.setPassword(rs.getString("password"));
        a.setCreatedAt(rs.getTimestamp("created_at"));
        return a;
    }
}
