package controller;

import database.DBConnection;
import model.Admin;
import model.Resident;
import model.Worker;
import view.AdminDashboard;
import view.LoginFrame;
import view.ResidentDashboard;
import view.WelcomeFrame;
import view.WorkerDashboard;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthController {

    public enum Role {
        RESIDENT, WORKER, ADMIN
    }

    public void login(String email, String password, Role role, JFrame parentFrame) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            JOptionPane.showMessageDialog(parentFrame, "Please enter both email and password.", "Login Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            boolean success = false;

            if (role == Role.RESIDENT) {
                Resident r = authenticateResident(email, password);
                if (r != null) {
                    new ResidentDashboard(r.getResidentId(), r.getFullName());
                    success = true;
                }
            } else if (role == Role.WORKER) {
                Worker w = authenticateWorker(email, password);
                if (w != null) {
                    new WorkerDashboard(w.getWorkerId(), w.getFullName());
                    success = true;
                }
            } else if (role == Role.ADMIN) {
                Admin a = authenticateAdmin(email, password);
                if (a != null) {
                    new AdminDashboard(a.getAdminId(), a.getFullName());
                    success = true;
                }
            }

            if (success) {
                if (parentFrame != null) parentFrame.dispose();
            } else {
                JOptionPane.showMessageDialog(parentFrame, "Invalid email or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            LoginFrame.showDbError(ex);
        }
    }

    public Resident authenticateResident(String email, String password) throws SQLException {
        String sql = "SELECT * FROM residents WHERE lower(email) = lower(?) AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
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
        }
        return null;
    }

    public Worker authenticateWorker(String email, String password) throws SQLException {
        String sql = "SELECT * FROM workers WHERE lower(email) = lower(?) AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
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
            }
        }
        return null;
    }

    public Admin authenticateAdmin(String email, String password) throws SQLException {
        String sql = "SELECT * FROM admins WHERE lower(email) = lower(?) AND password = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
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
        }
        return null;
    }

    public void logout(JFrame currentFrame) {
        new WelcomeFrame();
        if (currentFrame != null) {
            currentFrame.dispose();
        }
    }
}
