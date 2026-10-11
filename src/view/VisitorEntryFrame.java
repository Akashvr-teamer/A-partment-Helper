package view;

import database.DBConnection;
import util.CodeGenerator;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class VisitorEntryFrame extends JFrame {
    private final JTextField codeField = new JTextField();

    private static final Color COFFEE = new Color(55, 36, 27);
    private static final Color CREAM = new Color(250, 245, 235);

    public VisitorEntryFrame() {
        setTitle("Visitor Gate Desk");
        setSize(460, 470);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel(null);
        panel.setBackground(COFFEE);
        setContentPane(panel);

        JLabel title = new JLabel("VISITOR GATE DESK", SwingConstants.CENTER);
        title.setBounds(25, 35, 400, 40);
        title.setForeground(CREAM);
        title.setFont(new Font("Serif", Font.BOLD, 26));
        panel.add(title);

        JLabel sub = new JLabel("Enter the pass code to record entry or exit", SwingConstants.CENTER);
        sub.setBounds(25, 85, 400, 25);
        sub.setForeground(new Color(220, 195, 165));
        panel.add(sub);

        JLabel label = new JLabel("Access pass code");
        label.setBounds(45, 145, 350, 22);
        label.setForeground(CREAM);
        panel.add(label);

        codeField.setBounds(45, 173, 350, 45);
        codeField.setHorizontalAlignment(JTextField.CENTER);
        codeField.setFont(new Font("Monospaced", Font.BOLD, 20));
        panel.add(codeField);

        JButton entryBtn = LoginFrame.button("CONFIRM ENTRY");
        entryBtn.setBounds(45, 245, 350, 43);
        panel.add(entryBtn);

        JButton exitBtn = new JButton("MARK EXIT");
        exitBtn.setBounds(45, 305, 350, 40);
        exitBtn.setBackground(new Color(130, 76, 55));
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setFocusPainted(false);
        panel.add(exitBtn);

        JButton backBtn = new JButton("Back to home");
        backBtn.setBounds(130, 365, 180, 30);
        backBtn.setForeground(CREAM);
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        panel.add(backBtn);

        entryBtn.addActionListener(e -> handleEntry());
        exitBtn.addActionListener(e -> handleExit());
        backBtn.addActionListener(e -> {
            new WelcomeFrame();
            dispose();
        });

        setVisible(true);
    }

    // Handles visitor entry at gate
    private void handleEntry() {
        String code = codeField.getText().trim();
        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the visitor pass code.");
            return;
        }

        String hash = CodeGenerator.sha256(code);
        String selectSql = "SELECT visitor_id, visitor_name, status, has_vehicle, valid_from, valid_until, parking_slot " +
                           "FROM visitors WHERE access_hash = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(selectSql)) {

            ps.setString(1, hash);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this, "Invalid pass code.");
                    return;
                }

                int visitorId = rs.getInt("visitor_id");
                String name = rs.getString("visitor_name");
                String status = rs.getString("status");
                boolean hasVehicle = rs.getBoolean("has_vehicle");
                Timestamp validFrom = rs.getTimestamp("valid_from");
                Timestamp validUntil = rs.getTimestamp("valid_until");
                Timestamp now = new Timestamp(System.currentTimeMillis());

                if (!"PENDING".equalsIgnoreCase(status)) {
                    JOptionPane.showMessageDialog(this, "Pass cannot be used for entry. Current status: " + status);
                    return;
                }
                if (now.before(validFrom)) {
                    JOptionPane.showMessageDialog(this, "Pass is not valid yet. Valid from: " + validFrom);
                    return;
                }
                if (now.after(validUntil)) {
                    JOptionPane.showMessageDialog(this, "Pass has expired.");
                    return;
                }

                String assignedSlot = null;
                // If visitor arrives with vehicle, allocate an available visitor parking slot
                if (hasVehicle) {
                    String parkSql = "SELECT parking_id, slot_number FROM parking " +
                                     "WHERE parking_type = 'VISITOR' AND status = 'AVAILABLE' ORDER BY slot_number LIMIT 1";
                    try (PreparedStatement parkPs = con.prepareStatement(parkSql);
                         ResultSet parkRs = parkPs.executeQuery()) {

                        if (!parkRs.next()) {
                            JOptionPane.showMessageDialog(this, "No visitor parking slot available!");
                            return;
                        }

                        int parkingId = parkRs.getInt("parking_id");
                        assignedSlot = parkRs.getString("slot_number");

                        // Mark slot occupied
                        String updateParkSql = "UPDATE parking SET status = 'OCCUPIED', visitor_id = ? WHERE parking_id = ?";
                        try (PreparedStatement uPark = con.prepareStatement(updateParkSql)) {
                            uPark.setInt(1, visitorId);
                            uPark.setInt(2, parkingId);
                            uPark.executeUpdate();
                        }

                        // Save assigned slot on visitor record
                        String updateSlotSql = "UPDATE visitors SET parking_slot = ? WHERE visitor_id = ?";
                        try (PreparedStatement uSlot = con.prepareStatement(updateSlotSql)) {
                            uSlot.setString(1, assignedSlot);
                            uSlot.setInt(2, visitorId);
                            uSlot.executeUpdate();
                        }
                    }
                }

                // Update visitor status to ENTERED
                String updateEntrySql = "UPDATE visitors SET status = 'ENTERED', entry_time = CURRENT_TIMESTAMP WHERE visitor_id = ?";
                try (PreparedStatement uEntry = con.prepareStatement(updateEntrySql)) {
                    uEntry.setInt(1, visitorId);
                    uEntry.executeUpdate();
                }

                String msg = "Entry Confirmed! Welcome, " + name + "!";
                if (assignedSlot != null) {
                    msg += "\nAssigned Parking Slot: " + assignedSlot;
                }
                JOptionPane.showMessageDialog(this, msg);
                codeField.setText("");
            }
        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
        }
    }

    // Handles visitor exit at gate
    private void handleExit() {
        String code = codeField.getText().trim();
        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the visitor pass code.");
            return;
        }

        String hash = CodeGenerator.sha256(code);
        String selectSql = "SELECT visitor_id, visitor_name, status, parking_slot FROM visitors WHERE access_hash = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(selectSql)) {

            ps.setString(1, hash);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this, "Invalid pass code.");
                    return;
                }

                int visitorId = rs.getInt("visitor_id");
                String name = rs.getString("visitor_name");
                String status = rs.getString("status");
                String slot = rs.getString("parking_slot");

                if (!"ENTERED".equalsIgnoreCase(status)) {
                    JOptionPane.showMessageDialog(this, "Only visitors with status 'ENTERED' can check out. Current status: " + status);
                    return;
                }

                // Mark visitor status as EXITED
                String updateExitSql = "UPDATE visitors SET status = 'EXITED', exit_time = CURRENT_TIMESTAMP WHERE visitor_id = ?";
                try (PreparedStatement uExit = con.prepareStatement(updateExitSql)) {
                    uExit.setInt(1, visitorId);
                    uExit.executeUpdate();
                }

                // Free allocated parking slot if any
                if (slot != null && !slot.isBlank()) {
                    String freeParkSql = "UPDATE parking SET status = 'AVAILABLE', visitor_id = NULL WHERE slot_number = ? AND parking_type = 'VISITOR'";
                    try (PreparedStatement uFree = con.prepareStatement(freeParkSql)) {
                        uFree.setString(1, slot);
                        uFree.executeUpdate();
                    }
                }

                JOptionPane.showMessageDialog(this, "Exit Confirmed! Goodbye, " + name + "!");
                codeField.setText("");
            }
        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
        }
    }
}
