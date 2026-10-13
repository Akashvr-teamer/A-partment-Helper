package model.dao;

import database.DBConnection;
import model.Payment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    public List<Payment> getAll() throws SQLException {
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
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Payment> getRecent(int limit) throws SQLException {
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
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Payment> getByResident(int residentId) throws SQLException {
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
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public Payment getById(int paymentId) throws SQLException {
        String sql =
            "SELECT p.payment_id, p.resident_id, r.full_name AS resident_name, r.apartment_no, " +
            "       p.month_year, p.amount, p.due_date, p.payment_date, p.status, p.payment_method, " +
            "       p.payment_type, p.complaint_id " +
            "FROM payments p " +
            "JOIN residents r ON p.resident_id = r.resident_id " +
            "WHERE p.payment_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public int insertCustomBill(int residentId, String title, double amount, Date dueDate) throws SQLException {
        String sql = "INSERT INTO payments (resident_id, month_year, amount, due_date, status, payment_type) " +
                     "VALUES (?, ?, ?, ?::DATE, 'PENDING', 'CUSTOM')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, residentId);
            ps.setString(2, title);
            ps.setDouble(3, amount);
            ps.setDate(4, dueDate);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) return gk.getInt(1);
                }
            }
        }
        return 0;
    }

    public int insertWorkerCompletionBill(int residentId, int complaintId, String complaintTitle, double amount, Date dueDate) throws SQLException {
        String billTitle = "Repair Fee: " + complaintTitle;
        if (billTitle.length() > 255) {
            billTitle = billTitle.substring(0, 255);
        }

        String sql = "INSERT INTO payments (resident_id, month_year, amount, due_date, status, payment_type, complaint_id) " +
                     "VALUES (?, ?, ?, ?, 'PENDING', 'MAINTENANCE_WORK', ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, residentId);
            ps.setString(2, billTitle);
            ps.setDouble(3, amount);
            ps.setDate(4, dueDate);
            ps.setInt(5, complaintId);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) return gk.getInt(1);
                }
            }
        }
        return 0;
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

    private Payment mapRow(ResultSet rs) throws SQLException {
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
