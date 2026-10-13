package controller;

import model.Admin;
import model.Resident;
import model.Worker;
import model.dao.AdminDAO;
import model.dao.ResidentDAO;
import model.dao.WorkerDAO;
import view.AdminDashboard;
import view.LoginFrame;
import view.ResidentDashboard;
import view.WelcomeFrame;
import view.WorkerDashboard;

import javax.swing.*;
import java.sql.SQLException;

public class AuthController {
    private final ResidentDAO residentDAO = new ResidentDAO();
    private final WorkerDAO workerDAO = new WorkerDAO();
    private final AdminDAO adminDAO = new AdminDAO();

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
                Resident r = residentDAO.authenticate(email, password);
                if (r != null) {
                    new ResidentDashboard(r.getResidentId(), r.getFullName());
                    success = true;
                }
            } else if (role == Role.WORKER) {
                Worker w = workerDAO.authenticate(email, password);
                if (w != null) {
                    new WorkerDashboard(w.getWorkerId(), w.getFullName());
                    success = true;
                }
            } else if (role == Role.ADMIN) {
                Admin a = adminDAO.authenticate(email, password);
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

    public void logout(JFrame currentFrame) {
        new WelcomeFrame();
        if (currentFrame != null) {
            currentFrame.dispose();
        }
    }
}
