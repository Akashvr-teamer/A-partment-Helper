package model.dao;

import database.DBConnection;
import model.Resident;
import model.ResidentMetrics;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ResidentDAO {

    public Resident authenticate(String email, String password) throws SQLException {
        String sql = "SELECT * FROM residents WHERE lower(email) = lower(?) AND password = ?";
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

    public Resident getById(int residentId) throws SQLException {
        String sql = "SELECT * FROM residents WHERE resident_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, residentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Resident> getAll() throws SQLException {
        List<Resident> list = new ArrayList<>();
        String sql = "SELECT * FROM residents ORDER BY apartment_no ASC, resident_id ASC";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM residents";
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public boolean emailExists(String email) throws SQLException {
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

    public int insert(Resident r) throws SQLException {
        String sql = "INSERT INTO residents (full_name, email, phone, apartment_no, password, vehicle_no, parking_slot) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getFullName());
            ps.setString(2, r.getEmail().trim().toLowerCase());
            ps.setString(3, r.getPhone());
            ps.setString(4, r.getApartmentNo());
            ps.setString(5, r.getPassword());
            ps.setString(6, r.getVehicleNo() != null && !r.getVehicleNo().isBlank() ? r.getVehicleNo().trim() : null);
            ps.setString(7, r.getParkingSlot() != null && !r.getParkingSlot().isBlank() ? r.getParkingSlot().trim() : null);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        int genId = gk.getInt(1);
                        r.setResidentId(genId);
                        return genId;
                    }
                }
            }
        }
        return 0;
    }

    public boolean update(Resident r, boolean updatePassword) throws SQLException {
        String sql = updatePassword
                ? "UPDATE residents SET full_name = ?, phone = ?, apartment_no = ?, vehicle_no = ?, parking_slot = ?, password = ? WHERE resident_id = ?"
                : "UPDATE residents SET full_name = ?, phone = ?, apartment_no = ?, vehicle_no = ?, parking_slot = ? WHERE resident_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, r.getFullName());
            ps.setString(2, r.getPhone());
            ps.setString(3, r.getApartmentNo());
            ps.setString(4, r.getVehicleNo() != null && !r.getVehicleNo().isBlank() ? r.getVehicleNo().trim() : null);
            ps.setString(5, r.getParkingSlot() != null && !r.getParkingSlot().isBlank() ? r.getParkingSlot().trim() : null);
            if (updatePassword) {
                ps.setString(6, r.getPassword());
                ps.setInt(7, r.getResidentId());
            } else {
                ps.setInt(6, r.getResidentId());
            }
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int residentId) throws SQLException {
        String sql = "DELETE FROM residents WHERE resident_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, residentId);
            return ps.executeUpdate() > 0;
        }
    }

    public ResidentMetrics getMetrics(int residentId) throws SQLException {
        ResidentMetrics m = new ResidentMetrics();
        LocalDate today = LocalDate.now();

        try (Connection con = DBConnection.getConnection()) {
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

    private Resident mapRow(ResultSet rs) throws SQLException {
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
}
