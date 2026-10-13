package model.dao;

import database.DBConnection;
import model.Visitor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VisitorDAO {

    public List<Visitor> getByResident(int residentId) throws SQLException {
        List<Visitor> list = new ArrayList<>();
        String sql = "SELECT * FROM visitors WHERE resident_id = ? ORDER BY visitor_id DESC LIMIT 15";
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

    public int insert(Visitor v) throws SQLException {
        String sql = "INSERT INTO visitors(resident_id, visitor_name, phone, has_vehicle, vehicle_no, access_hash, valid_from, valid_until, status) " +
                     "VALUES(?, ?, ?, ?, ?, ?, ?, ?, 'PENDING')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, v.getResidentId());
            ps.setString(2, v.getVisitorName());
            ps.setString(3, v.getPhone());
            ps.setBoolean(4, v.isHasVehicle());
            ps.setString(5, v.isHasVehicle() ? v.getVehicleNo() : null);
            ps.setString(6, v.getAccessHash());
            ps.setTimestamp(7, v.getValidFrom());
            ps.setTimestamp(8, v.getValidUntil());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        int genId = gk.getInt(1);
                        v.setVisitorId(genId);
                        return genId;
                    }
                }
            }
        }
        return 0;
    }

    public Visitor getByAccessHash(String hash) throws SQLException {
        String sql = "SELECT * FROM visitors WHERE access_hash = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Visitor getByExitCodeOrHash(String code, String hash) throws SQLException {
        String sql = "SELECT * FROM visitors WHERE UPPER(exit_code) = ? OR access_hash = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code != null ? code.toUpperCase() : "");
            ps.setString(2, hash != null ? hash : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public boolean markEntered(int visitorId, String exitCode, String parkingSlot) throws SQLException {
        String sql = "UPDATE visitors SET status = 'ENTERED', entry_time = CURRENT_TIMESTAMP, " +
                     "parking_slot = ?, exit_code = ? WHERE visitor_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, parkingSlot);
            ps.setString(2, exitCode);
            ps.setInt(3, visitorId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean markExited(int visitorId) throws SQLException {
        String sql = "UPDATE visitors SET status = 'EXITED', exit_time = CURRENT_TIMESTAMP WHERE visitor_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, visitorId);
            return ps.executeUpdate() > 0;
        }
    }

    private Visitor mapRow(ResultSet rs) throws SQLException {
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
}
