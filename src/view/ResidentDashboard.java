package view;

import database.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ResidentDashboard extends JFrame {

    private int residentId;
    private String residentName;

    private JLabel parkingVal;
    private JLabel visitorsVal;
    private JLabel maintenanceVal;

    private DefaultTableModel tableModel;

    public ResidentDashboard() {
        this(1, "Akash Kumar");
    }

    public ResidentDashboard(int residentId, String residentName) {
        this.residentId = residentId;
        this.residentName = residentName;

        setTitle("Resident Dashboard");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel main = new JPanel(null);
        main.setBackground(new Color(243, 244, 246));
        add(main);

        // ---------- SIDEBAR ----------
        JPanel side = new JPanel(null);
        side.setBounds(0, 0, 180, 600);
        side.setBackground(new Color(17, 24, 39));
        main.add(side);

        JLabel logo = new JLabel("SASMS");
        logo.setBounds(50, 25, 100, 30);
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("SansSerif", Font.BOLD, 24));
        side.add(logo);

        side.add(menuButton("Dashboard", 70, true));
        side.add(menuButton("Visitors", 120, false));
        side.add(menuButton("Complaints", 170, false));
        side.add(menuButton("Parking", 220, false));
        side.add(menuButton("Payments", 270, false));

        JButton logout = new JButton("Logout");
        logout.setBounds(30, 510, 120, 35);
        logout.addActionListener(e -> {
            new WelcomeFrame();
            dispose();
        });
        side.add(logout);

        // ---------- HEADER ----------
        JLabel welcome = new JLabel("Welcome, " + residentName + " 👋");
        welcome.setBounds(210, 20, 400, 30);
        welcome.setFont(new Font("SansSerif", Font.BOLD, 24));
        main.add(welcome);

        // ---------- CARDS ----------
        JPanel card1 = createCard("My Parking", "...", new Color(219, 234, 254), 210, 70);
        parkingVal = (JLabel) card1.getComponent(1);
        main.add(card1);

        JPanel card2 = createCard("Visitors", "0", new Color(209, 250, 229), 430, 70);
        visitorsVal = (JLabel) card2.getComponent(1);
        main.add(card2);

        JPanel card3 = createCard("Maintenance", "₹0", new Color(252, 231, 243), 650, 70);
        maintenanceVal = (JLabel) card3.getComponent(1);
        main.add(card3);

        // ---------- VISITOR PANEL ----------
        JPanel visitorsPanel = new JPanel(null);
        visitorsPanel.setBounds(210, 200, 620, 330);
        visitorsPanel.setBackground(Color.WHITE);
        visitorsPanel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        JLabel t = new JLabel("My Visitors");
        t.setBounds(20, 15, 200, 25);
        t.setFont(new Font("SansSerif", Font.BOLD, 18));
        visitorsPanel.add(t);

        // Table with columns
        String[] columns = {"Visitor", "Parking", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setRowHeight(25);
        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(20, 50, 580, 220);
        visitorsPanel.add(sp);

        JButton invite = new JButton("Invite Visitor");
        invite.setBounds(420, 285, 180, 32);
        invite.setBackground(new Color(37, 99, 235));
        invite.setForeground(Color.WHITE);
        invite.setFont(new Font("SansSerif", Font.BOLD, 14));
        invite.addActionListener(e -> new InviteVisitorFrame(residentId, this));
        visitorsPanel.add(invite);

        main.add(visitorsPanel);

        // Load dynamic data from PostgreSQL database
        loadDashboardData();

        setVisible(true);
    }

    public void loadDashboardData() {
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;

            // 1. Get Parking Slot from residents table
            String sqlResident = "SELECT parking_slot FROM residents WHERE resident_id = ?";
            try (PreparedStatement ps = con.prepareStatement(sqlResident)) {
                ps.setInt(1, residentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String slot = rs.getString("parking_slot");
                        parkingVal.setText(slot != null ? slot : "None");
                    }
                }
            }

            // 2. Load Visitors into Table & count
            tableModel.setRowCount(0);
            int count = 0;
            String sqlVisitors = "SELECT visitor_name, COALESCE(parking_slot, 'None') as slot, status FROM visitors WHERE resident_id = ? ORDER BY visitor_id DESC";
            try (PreparedStatement ps = con.prepareStatement(sqlVisitors)) {
                ps.setInt(1, residentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        tableModel.addRow(new Object[]{
                                rs.getString("visitor_name"),
                                rs.getString("slot"),
                                rs.getString("status")
                        });
                        count++;
                    }
                }
            }
            visitorsVal.setText(String.valueOf(count));

            // 3. Get Maintenance Due
            String sqlPayment = "SELECT amount FROM payments WHERE resident_id = ? AND status = 'PENDING' LIMIT 1";
            try (PreparedStatement ps = con.prepareStatement(sqlPayment)) {
                ps.setInt(1, residentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        maintenanceVal.setText("₹" + (int) rs.getDouble("amount"));
                    } else {
                        maintenanceVal.setText("₹0");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JButton menuButton(String text, int y, boolean active) {
        JButton b = new JButton(text);
        b.setBounds(20, y, 140, 35);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);

        if (active) {
            b.setBackground(new Color(37, 99, 235));
            b.setForeground(Color.WHITE);
        } else {
            b.setBackground(new Color(17, 24, 39));
            b.setForeground(new Color(210, 210, 210));
        }

        return b;
    }

    private JPanel createCard(String title, String value, Color color, int x, int y) {
        JPanel card = new JPanel(null);
        card.setBounds(x, y, 180, 100);
        card.setBackground(color);

        JLabel t = new JLabel(title);
        t.setBounds(15, 10, 150, 20);
        t.setFont(new Font("SansSerif", Font.PLAIN, 15));

        JLabel v = new JLabel(value);
        v.setBounds(15, 40, 150, 35);
        v.setFont(new Font("SansSerif", Font.BOLD, 26));

        card.add(t);
        card.add(v);

        return card;
    }
}