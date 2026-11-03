package controller;

import database.DBConnection;
import model.Visitor;
import util.CodeGenerator;

import java.sql.*;

public class VisitorController {

    public static class EntryResult {
        public final boolean success;
        public final String message;
        public final String visitorName;
        public final String parkingSlot;
        public final String exitCode;

        public EntryResult(boolean success, String message, String visitorName, String parkingSlot, String exitCode) {
            this.success = success;
            this.message = message;
            this.visitorName = visitorName;
            this.parkingSlot = parkingSlot;
            this.exitCode = exitCode;
        }
    }

    public static class ExitResult {
        public final boolean success;
        public final String message;
        public final String visitorName;
        public final String freedParkingSlot;

        public ExitResult(boolean success, String message, String visitorName, String freedParkingSlot) {
            this.success = success;
            this.message = message;
            this.visitorName = visitorName;
            this.freedParkingSlot = freedParkingSlot;
        }
    }

    public EntryResult processEntry(String passCode) throws SQLException {
        if (passCode == null || passCode.isBlank()) {
            return new EntryResult(false, "Please enter the visitor pass code.", null, null, null);
        }

        String hash = CodeGenerator.sha256(passCode.trim());
        Visitor v = getByAccessHash(hash);
        if (v == null) {
            return new EntryResult(false, "Invalid pass code.", null, null, null);
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        if (!"PENDING".equalsIgnoreCase(v.getStatus())) {
            return new EntryResult(false, "Pass cannot be used for entry. Current status: " + v.getStatus(), null, null, null);
        }
        if (now.before(v.getValidFrom())) {
            return new EntryResult(false, "Pass is not valid yet. Valid from: " + v.getValidFrom(), null, null, null);
        }
        if (now.after(v.getValidUntil())) {
            return new EntryResult(false, "Pass has expired.", null, null, null);
        }

        String assignedSlot = null;
        if (v.isHasVehicle()) {
            assignedSlot = allocateVisitorParking(v.getVisitorId());
        }

        String exitCode = CodeGenerator.generateExitCode();
        boolean ok = markEntered(v.getVisitorId(), exitCode, assignedSlot);
        if (!ok) {
            return new EntryResult(false, "Failed to record visitor entry.", null, null, null);
        }

        return new EntryResult(true, "Entry confirmed", v.getVisitorName(), assignedSlot, exitCode);
    }

    public ExitResult processExit(String code) throws SQLException {
        if (code == null || code.isBlank()) {
            return new ExitResult(false, "Please enter the visitor exit code or pass code.", null, null);
        }

        String trimmed = code.trim();
        String hash = CodeGenerator.sha256(trimmed);
        Visitor v = getByExitCodeOrHash(trimmed, hash);
        if (v == null) {
            return new ExitResult(false, "Invalid code. Please enter the 3-character exit code or pass code.", null, null);
        }

        if (!"ENTERED".equalsIgnoreCase(v.getStatus())) {
            return new ExitResult(false, "Only visitors with status 'ENTERED' can check out. Current status: " + v.getStatus(), null, null);
        }

        boolean ok = markExited(v.getVisitorId());
        if (!ok) {
            return new ExitResult(false, "Failed to record visitor exit.", null, null);
        }

        String freedSlot = null;
        if (v.getParkingSlot() != null && !v.getParkingSlot().isBlank() && !"PARK OUTSIDE".equalsIgnoreCase(v.getParkingSlot())) {
            freeVisitorParking(v.getParkingSlot());
            freedSlot = v.getParkingSlot();
        }

        return new ExitResult(true, "Exit confirmed", v.getVisitorName(), freedSlot);
    }

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

    public Visitor getByAccessHash(String hash) throws SQLException {
        String sql = "SELECT * FROM visitors WHERE access_hash = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapVisitorRow(rs);
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
                    return mapVisitorRow(rs);
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
}
