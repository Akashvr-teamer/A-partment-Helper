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
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        panel.add(title);

        JLabel sub = new JLabel("Enter the pass code to record entry or exit", SwingConstants.CENTER);
        sub.setBounds(25, 85, 400, 25);
        sub.setForeground(new Color(220, 195, 165));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(sub);

        JLabel label = new JLabel("Pass code (4-char) or Exit code (3-char)");
        label.setBounds(45, 145, 350, 22);
        label.setForeground(CREAM);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
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
        exitBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        exitBtn.setFocusPainted(false);
        panel.add(exitBtn);

        JButton backBtn = new JButton("Back to home");
        backBtn.setBounds(130, 365, 180, 30);
        backBtn.setForeground(CREAM);
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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
                // If visitor arrives with vehicle, allocate an available visitor parking slot randomly
                if (hasVehicle) {
                    String parkSql = "SELECT parking_id, slot_number FROM parking " +
                                     "WHERE parking_type = 'VISITOR' AND status = 'AVAILABLE' ORDER BY RANDOM() LIMIT 1";
                    try (PreparedStatement parkPs = con.prepareStatement(parkSql);
                         ResultSet parkRs = parkPs.executeQuery()) {

                        if (parkRs.next()) {
                            int parkingId = parkRs.getInt("parking_id");
                            assignedSlot = parkRs.getString("slot_number");

                            // Mark slot occupied
                            String updateParkSql = "UPDATE parking SET status = 'OCCUPIED', visitor_id = ? WHERE parking_id = ?";
                            try (PreparedStatement uPark = con.prepareStatement(updateParkSql)) {
                                uPark.setInt(1, visitorId);
                                uPark.setInt(2, parkingId);
                                uPark.executeUpdate();
                            }
                        } else {
                            // Real-life handling: Visitor parking is full!
                            assignedSlot = "PARK OUTSIDE";
                        }
                    }
                }

                // Generate 3-character exit code for fast checkout
                String exitCode = CodeGenerator.generateExitCode();

                // Update visitor status to ENTERED, record parking slot and exit code
                String updateEntrySql = "UPDATE visitors SET status = 'ENTERED', entry_time = CURRENT_TIMESTAMP, " +
                                        "parking_slot = ?, exit_code = ? WHERE visitor_id = ?";
                try (PreparedStatement uEntry = con.prepareStatement(updateEntrySql)) {
                    uEntry.setString(1, assignedSlot);
                    uEntry.setString(2, exitCode);
                    uEntry.setInt(3, visitorId);
                    uEntry.executeUpdate();
                }

                StringBuilder msg = new StringBuilder();
                msg.append("ENTRY CONFIRMED!\n\nWelcome, ").append(name).append("!\n\n");
                if (assignedSlot != null) {
                    if ("PARK OUTSIDE".equals(assignedSlot)) {
                        msg.append("Parking: VISITOR PARKING FULL\n(Advise visitor to park outside the premises)\n\n");
                    } else {
                        msg.append("Assigned Parking Slot: ").append(assignedSlot).append("\n\n");
                    }
                }
                msg.append("3-Character Exit Code: ").append(exitCode).append("\n\n");
                msg.append("Please give this 3-character exit code (").append(exitCode).append(") to the guard when leaving.");

                JOptionPane.showMessageDialog(this, msg.toString());
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
            JOptionPane.showMessageDialog(this, "Please enter the visitor exit code or pass code.");
            return;
        }

        String hash = CodeGenerator.sha256(code);
        // Look up by 3-character exit code OR by original pass hash
        String selectSql = "SELECT visitor_id, visitor_name, status, parking_slot, exit_code " +
                           "FROM visitors WHERE UPPER(exit_code) = ? OR access_hash = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(selectSql)) {

            ps.setString(1, code.toUpperCase());
            ps.setString(2, hash);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this, "Invalid code. Please enter the 3-character exit code or pass code.");
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

                // Free allocated visitor parking slot if one was assigned inside
                if (slot != null && !slot.isBlank() && !"PARK OUTSIDE".equalsIgnoreCase(slot)) {
                    String freeParkSql = "UPDATE parking SET status = 'AVAILABLE', visitor_id = NULL " +
                                         "WHERE slot_number = ? AND parking_type = 'VISITOR'";
                    try (PreparedStatement uFree = con.prepareStatement(freeParkSql)) {
                        uFree.setString(1, slot);
                        uFree.executeUpdate();
                    }
                }

                StringBuilder msg = new StringBuilder();
                msg.append("EXIT CONFIRMED!\n\nGoodbye, ").append(name).append("!");
                if (slot != null && !slot.isBlank() && !"PARK OUTSIDE".equalsIgnoreCase(slot)) {
                    msg.append("\nVisitor parking slot ").append(slot).append(" is now AVAILABLE.");
                }
                JOptionPane.showMessageDialog(this, msg.toString());
                codeField.setText("");
            }
        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
        }
    }
}
