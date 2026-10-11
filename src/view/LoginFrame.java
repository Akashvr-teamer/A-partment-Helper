package view;

import database.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginFrame extends JFrame {
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JRadioButton residentBtn = new JRadioButton("Resident", true);
    private final JRadioButton workerBtn = new JRadioButton("Worker");
    private final JRadioButton adminBtn = new JRadioButton("Admin");

    // Theme Colors
    private static final Color COFFEE = new Color(55, 36, 27);
    private static final Color AMBER = new Color(190, 124, 58);
    private static final Color CREAM = new Color(250, 245, 235);

    public LoginFrame() {
        setTitle("A-partment Helper | Login");
        setSize(440, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel(null);
        panel.setBackground(COFFEE);
        setContentPane(panel);

        JLabel title = new JLabel("WELCOME BACK", SwingConstants.CENTER);
        title.setBounds(35, 35, 360, 40);
        title.setForeground(CREAM);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        panel.add(title);

        JLabel subtitle = new JLabel("Sign in to A-partment Helper", SwingConstants.CENTER);
        subtitle.setBounds(35, 75, 360, 24);
        subtitle.setForeground(new Color(220, 195, 165));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(subtitle);

        addLabel(panel, "Email address", 50, 130);
        emailField.setBounds(50, 155, 330, 38);
        panel.add(emailField);

        addLabel(panel, "Password", 50, 210);
        passwordField.setBounds(50, 235, 330, 38);
        panel.add(passwordField);

        // Radio buttons for selecting user role
        ButtonGroup group = new ButtonGroup();
        group.add(residentBtn);
        group.add(workerBtn);
        group.add(adminBtn);
        styleRadio(residentBtn);
        styleRadio(workerBtn);
        styleRadio(adminBtn);
        residentBtn.setBounds(50, 295, 105, 30);
        workerBtn.setBounds(165, 295, 95, 30);
        adminBtn.setBounds(275, 295, 95, 30);
        panel.add(residentBtn);
        panel.add(workerBtn);
        panel.add(adminBtn);

        JButton login = button("SIGN IN");
        login.setBounds(50, 355, 330, 45);
        panel.add(login);
        login.addActionListener(e -> authenticate());

        JButton back = new JButton("Back to home");
        back.setBounds(125, 425, 180, 32);
        back.setForeground(CREAM);
        back.setContentAreaFilled(false);
        back.setBorderPainted(false);
        panel.add(back);
        back.addActionListener(e -> {
            new WelcomeFrame();
            dispose();
        });

        getRootPane().setDefaultButton(login);
        setVisible(true);
    }

    // Authenticates resident, worker, or admin login against database
    private void authenticate() {
        String email = emailField.getText().trim().toLowerCase();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both email and password.");
            return;
        }

        String table;
        String idCol;
        if (residentBtn.isSelected()) {
            table = "residents";
            idCol = "resident_id";
        } else if (workerBtn.isSelected()) {
            table = "workers";
            idCol = "worker_id";
        } else {
            table = "admins";
            idCol = "admin_id";
        }

        String sql = "SELECT " + idCol + ", full_name, password FROM " + table + " WHERE lower(email) = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && password.equals(rs.getString("password"))) {
                    int id = rs.getInt(idCol);
                    String name = rs.getString("full_name");

                    if (residentBtn.isSelected()) {
                        new ResidentDashboard(id, name);
                    } else if (workerBtn.isSelected()) {
                        new WorkerDashboard(id, name);
                    } else {
                        new AdminDashboard(id, name);
                    }
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid email or password.");
                }
            }
        } catch (Exception ex) {
            showDbError(ex);
        }
    }

    // Helper to display error popup
    public static void showDbError(Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(null, "Database Error: " + ex.getMessage(),
                "Database error", JOptionPane.ERROR_MESSAGE);
    }

    // Helper to create themed buttons
    public static JButton button(String text) {
        JButton b = new JButton(text);
        b.setBackground(AMBER);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static void addLabel(JPanel p, String s, int x, int y) {
        JLabel l = new JLabel(s);
        l.setBounds(x, y, 300, 20);
        l.setForeground(CREAM);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        p.add(l);
    }

    private static void styleRadio(JRadioButton b) {
        b.setOpaque(false);
        b.setForeground(CREAM);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setFocusPainted(false);
    }
}
