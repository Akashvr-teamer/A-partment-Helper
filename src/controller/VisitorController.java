package controller;

import model.Visitor;
import model.dao.ParkingDAO;
import model.dao.VisitorDAO;
import util.CodeGenerator;

import java.sql.SQLException;
import java.sql.Timestamp;

public class VisitorController {
    private final VisitorDAO visitorDAO = new VisitorDAO();
    private final ParkingDAO parkingDAO = new ParkingDAO();

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
        Visitor v = visitorDAO.getByAccessHash(hash);
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
            assignedSlot = parkingDAO.allocateVisitorParking(v.getVisitorId());
        }

        String exitCode = CodeGenerator.generateExitCode();
        boolean ok = visitorDAO.markEntered(v.getVisitorId(), exitCode, assignedSlot);
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
        Visitor v = visitorDAO.getByExitCodeOrHash(trimmed, hash);
        if (v == null) {
            return new ExitResult(false, "Invalid code. Please enter the 3-character exit code or pass code.", null, null);
        }

        if (!"ENTERED".equalsIgnoreCase(v.getStatus())) {
            return new ExitResult(false, "Only visitors with status 'ENTERED' can check out. Current status: " + v.getStatus(), null, null);
        }

        boolean ok = visitorDAO.markExited(v.getVisitorId());
        if (!ok) {
            return new ExitResult(false, "Failed to record visitor exit.", null, null);
        }

        String freedSlot = null;
        if (v.getParkingSlot() != null && !v.getParkingSlot().isBlank() && !"PARK OUTSIDE".equalsIgnoreCase(v.getParkingSlot())) {
            parkingDAO.freeVisitorParking(v.getParkingSlot());
            freedSlot = v.getParkingSlot();
        }

        return new ExitResult(true, "Exit confirmed", v.getVisitorName(), freedSlot);
    }
}
