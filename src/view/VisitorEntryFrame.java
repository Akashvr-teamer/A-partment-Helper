package view;

import database.DBConnection;
import util.CodeGenerator;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class VisitorEntryFrame extends JFrame {

    private JTextField codeField;

    public VisitorEntryFrame() {
        setTitle("Visitor Entry");
        setSize(420, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(8, 23, 53));
        add(panel);

        JLabel title = new JLabel("VISITOR ENTRY");
        title.setBounds(60, 35, 300, 35);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(title);

        JLabel subtitle = new JLabel("Enter Access Pass Code to Check In");
        subtitle.setBounds(50, 75, 320, 20);
        subtitle.setForeground(new Color(180, 200, 255));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(subtitle);

        JLabel codeLabel = new JLabel("Pass Code (e.g. ABCD-1234)");
        codeLabel.setBounds(50, 130, 300, 20);
        codeLabel.setForeground(Color.WHITE);
        codeLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(codeLabel);

        codeField = new JTextField();
        codeField.setBounds(50, 155, 320, 42);
        codeField.setFont(new Font("Monospaced", Font.BOLD, 20));
        codeField.setHorizontalAlignment(JTextField.CENTER);
        panel.add(codeField);

        JButton confirmBtn = new JButton("CONFIRM ENTRY");
        confirmBtn.setBounds(50, 220, 320, 45);
        confirmBtn.setBackground(new Color(16, 185, 129));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        confirmBtn.setFocusPainted(false);
        panel.add(confirmBtn);

        JButton exitBtn = new JButton("MARK EXIT");
        exitBtn.setBounds(50, 280, 320, 40);
        exitBtn.setBackground(new Color(239, 68, 68));
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        exitBtn.setFocusPainted(false);
        panel.add(exitBtn);

        JButton backBtn = new JButton("Back to Main Menu");
        backBtn.setBounds(110, 350, 200, 35);
        panel.add(backBtn);

        confirmBtn.addActionListener(e -> updateVisitorStatus("ENTERED"));
        exitBtn.addActionListener(e -> updateVisitorStatus("EXITED"));

        backBtn.addActionListener(e -> {
            new WelcomeFrame();
            dispose();
        });

        setVisible(true);
    }

    private void updateVisitorStatus(String targetStatus) {
        String code = codeField.getText().trim();
        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your access pass code.");
            return;
        }

        String hash = CodeGenerator.sha256(code);

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Database Connection Failed!");
                return;
            }

            // 1. Look up visitor by access code hash
            String sql = "SELECT visitor_id, visitor_name, status, parking_slot FROM visitors WHERE access_hash=?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, hash);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        JOptionPane.showMessageDialog(this, "Invalid Pass Code! No visitor found.");
                        return;
                    }

                    int id = rs.getInt("visitor_id");
                    String name = rs.getString("visitor_name");
                    String currentStatus = rs.getString("status");
                    String parking = rs.getString("parking_slot");

                    // Handle Entry
                    if (targetStatus.equals("ENTERED")) {
                        if ("ENTERED".equalsIgnoreCase(currentStatus)) {
                            JOptionPane.showMessageDialog(this, "Visitor " + name + " has already entered!");
                            return;
                        }
                        if ("EXITED".equalsIgnoreCase(currentStatus)) {
                            JOptionPane.showMessageDialog(this, "This pass was already used and the visitor exited.");
                            return;
                        }

                        // Mark as ENTERED
                        String update = "UPDATE visitors SET status='ENTERED', entry_time=CURRENT_TIMESTAMP WHERE visitor_id=?";
                        try (PreparedStatement up = con.prepareStatement(update)) {
                            up.setInt(1, id);
                            up.executeUpdate();
                        }

                        String msg = "Entry Confirmed!\n\nWelcome to the Society, " + name + "!";
                        if (parking != null && !parking.isEmpty()) {
                            msg += "\nVisitor Parking: " + parking;
                        }
                        JOptionPane.showMessageDialog(this, msg);
                        codeField.setText("");

                    // Handle Exit
                    } else if (targetStatus.equals("EXITED")) {
                        if (!"ENTERED".equalsIgnoreCase(currentStatus)) {
                            JOptionPane.showMessageDialog(this, "Cannot mark exit: visitor is currently marked as " + currentStatus);
                            return;
                        }

                        // Mark as EXITED
                        String update = "UPDATE visitors SET status='EXITED', exit_time=CURRENT_TIMESTAMP WHERE visitor_id=?";
                        try (PreparedStatement up = con.prepareStatement(update)) {
                            up.setInt(1, id);
                            up.executeUpdate();
                        }

                        JOptionPane.showMessageDialog(this, "Exit Confirmed!\nGoodbye, " + name + "!");
                        codeField.setText("");
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}