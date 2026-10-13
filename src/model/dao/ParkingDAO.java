package model.dao;

import database.DBConnection;
import model.Parking;

import java.sql.*;

public class ParkingDAO {

    public String allocateVisitorParking(int visitorId) throws SQLException {
        String parkSql = "SELECT parking_id, slot_number FROM parking " +
                         "WHERE parking_type = 'VISITOR' AND status = 'AVAILABLE' ORDER BY RANDOM() LIMIT 1";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(parkSql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                int parkingId = rs.getInt("parking_id");
                String slot = rs.getString("slot_number");

                String updateSql = "UPDATE parking SET status = 'OCCUPIED', visitor_id = ? WHERE parking_id = ?";
                try (PreparedStatement ups = con.prepareStatement(updateSql)) {
                    ups.setInt(1, visitorId);
                    ups.setInt(2, parkingId);
                    ups.executeUpdate();
                }
                return slot;
            } else {
                return "PARK OUTSIDE";
            }
        }
    }

    public boolean freeVisitorParking(String slotNumber) throws SQLException {
        if (slotNumber == null || slotNumber.isBlank() || "PARK OUTSIDE".equalsIgnoreCase(slotNumber)) {
            return false;
        }

        String sql = "UPDATE parking SET status = 'AVAILABLE', visitor_id = NULL " +
                     "WHERE slot_number = ? AND parking_type = 'VISITOR'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, slotNumber);
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
}
