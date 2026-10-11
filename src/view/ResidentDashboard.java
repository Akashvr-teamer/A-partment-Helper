package view;

import database.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ResidentDashboard extends JFrame {
    private final int residentId;
    private final String residentName;

    // Stat card labels
    private final JLabel parkingVal = new JLabel("—");
    private final JLabel visitorsVal = new JLabel("0");
    private final JLabel maintenanceVal = new JLabel("₹0");
    private final JLabel statusLabel = new JLabel(" ");

    // Table model for recent visitors
    private final DefaultTableModel visitorModel = new DefaultTableModel(
            new String[] { "Visitor", "Phone", "Parking", "Status", "Valid Until" }, 0) {
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private static final Color COFFEE = new Color(55, 36, 27);
    private static final Color CREAM = new Color(250, 245, 235);
    private static final Color LINE = new Color(229, 216, 199);

    public ResidentDashboard(int id, String name) {
        this.residentId = id;
        this.residentName = name;

        setTitle("A-partment Helper | Resident Dashboard");
        setSize(1180, 760);
        setMinimumSize(new Dimension(940, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CREAM);
        setContentPane(root);

        // Sidebar Navigation
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(205, 10));
        sidebar.setBackground(COFFEE);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        root.add(sidebar, BorderLayout.WEST);

        JLabel logo = new JLabel("  A-partment");
        logo.setForeground(CREAM);
        logo.setFont(new Font("Serif", Font.BOLD, 24));
        logo.setBorder(BorderFactory.createEmptyBorder(26, 10, 34, 5));
        sidebar.add(logo);

        sidebar.add(createMenuItem("Overview", this::loadDashboardData));
        sidebar.add(createMenuItem("Complaints", () -> new ComplaintsFrame(residentId)));
        sidebar.add(createMenuItem("Payments", () -> new PaymentsFrame(residentId)));
        sidebar.add(createMenuItem("Refresh data", this::loadDashboardData));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(155, 40));
        logoutBtn.setBackground(new Color(95, 67, 48));
        logoutBtn.setForeground(CREAM);
        logoutBtn.setFocusPainted(false);
        logoutBtn.addActionListener(e -> {
            new WelcomeFrame();
            dispose();
        });

        sidebar.add(Box.createVerticalGlue());
        JPanel logoutPanel = new JPanel();
        logoutPanel.setOpaque(false);
        logoutPanel.setBorder(BorderFactory.createEmptyBorder(24, 0, 22, 0));
        logoutPanel.add(logoutBtn);
        sidebar.add(logoutPanel);

        // Main Dashboard Content
        JPanel content = new JPanel(new BorderLayout(18, 18));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(26, 28, 24, 28));
        root.add(content, BorderLayout.CENTER);

        // Top Header
        JPanel header = new JPanel(new BorderLayout(10, 8));
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel welcomeLabel = new JLabel("Welcome back, " + residentName);
        welcomeLabel.setFont(new Font("Serif", Font.BOLD, 29));
        welcomeLabel.setForeground(COFFEE);

        JLabel subLabel = new JLabel("Your apartment at a glance");
        subLabel.setForeground(new Color(126, 105, 86));
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));

        titlePanel.add(welcomeLabel);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subLabel);
        header.add(titlePanel, BorderLayout.WEST);

        JButton inviteBtn = LoginFrame.button("+  INVITE VISITOR");
        inviteBtn.setPreferredSize(new Dimension(175, 42));
        inviteBtn.addActionListener(e -> new InviteVisitorFrame(residentId, this));
        header.add(inviteBtn, BorderLayout.EAST);

        // Stat Summary Cards
        JPanel cards = new JPanel(new GridLayout(1, 3, 16, 0));
        cards.setOpaque(false);
        cards.add(createCard("MY PARKING", parkingVal));
        cards.add(createCard("VISITORS INVITED", visitorsVal));
        cards.add(createCard("MAINTENANCE DUE", maintenanceVal));

        JPanel topSection = new JPanel(new BorderLayout(0, 20));
        topSection.setOpaque(false);
        topSection.add(header, BorderLayout.NORTH);
        topSection.add(cards, BorderLayout.CENTER);
        content.add(topSection, BorderLayout.NORTH);

        // Visitors Table
        JPanel tablePanel = new JPanel(new BorderLayout(0, 12));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(18, 18, 14, 18)));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        JLabel tableTitle = new JLabel("Recent visitor passes");
        tableTitle.setFont(new Font("Serif", Font.BOLD, 22));
        tableTitle.setForeground(COFFEE);
        tableHeader.add(tableTitle, BorderLayout.WEST);

        statusLabel.setForeground(new Color(126, 105, 86));
        tableHeader.add(statusLabel, BorderLayout.EAST);
        tablePanel.add(tableHeader, BorderLayout.NORTH);

        JTable table = new JTable(visitorModel);
        styleTable(table);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel noteLabel = new JLabel("Vehicle visitor parking is assigned automatically when the pass is confirmed at the gate.");
        noteLabel.setForeground(new Color(115, 95, 78));
        noteLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        tablePanel.add(noteLabel, BorderLayout.SOUTH);

        content.add(tablePanel, BorderLayout.CENTER);

        // Load data on open
        loadDashboardData();
        setVisible(true);
    }

    private JButton createMenuItem(String text, Runnable action) {
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(205, 44));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createEmptyBorder(9, 27, 9, 8));
        b.setBackground(COFFEE);
        b.setForeground(CREAM);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.addActionListener(e -> action.run());
        return b;
    }

    private JPanel createCard(String title, JLabel valueLabel) {
        JPanel p = new JPanel(new BorderLayout(0, 13));
        p.setBackground(new Color(242, 231, 215));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(233, 218, 197)),
                BorderFactory.createEmptyBorder(17, 19, 17, 15)));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setForeground(new Color(115, 82, 56));
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 12));

        valueLabel.setForeground(COFFEE);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 26));

        p.add(titleLbl, BorderLayout.NORTH);
        p.add(valueLabel, BorderLayout.CENTER);
        return p;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(32);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(242, 235, 226));
        table.setSelectionBackground(new Color(242, 225, 201));
        table.setSelectionForeground(COFFEE);
        table.getTableHeader().setBackground(new Color(239, 226, 209));
        table.getTableHeader().setForeground(COFFEE);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
    }

    // Fetches parking slot, visitor list, and pending maintenance balance
    public void loadDashboardData() {
        statusLabel.setText("Updating...");
        try (Connection con = DBConnection.getConnection()) {

            // 1. Fetch parking slot
            String slotSql = "SELECT parking_slot FROM residents WHERE resident_id = ?";
            try (PreparedStatement ps = con.prepareStatement(slotSql)) {
                ps.setInt(1, residentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getString(1) != null) {
                        parkingVal.setText(rs.getString(1));
                    } else {
                        parkingVal.setText("Not assigned");
                    }
                }
            }

            // 2. Fetch visitor records
            visitorModel.setRowCount(0);
            int count = 0;
            String visitorSql = "SELECT visitor_name, phone, COALESCE(parking_slot, '—') AS slot, status, valid_until " +
                                "FROM visitors WHERE resident_id = ? ORDER BY visitor_id DESC";
            try (PreparedStatement ps = con.prepareStatement(visitorSql)) {
                ps.setInt(1, residentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        visitorModel.addRow(new Object[] {
                            rs.getString("visitor_name"),
                            rs.getString("phone"),
                            rs.getString("slot"),
                            rs.getString("status"),
                            rs.getTimestamp("valid_until")
                        });
                        count++;
                    }
                }
            }
            visitorsVal.setText(String.valueOf(count));

            // 3. Fetch maintenance due amount
            String dueSql = "SELECT COALESCE(SUM(amount), 0) FROM payments " +
                            "WHERE resident_id = ? AND status IN ('PENDING', 'OVERDUE')";
            try (PreparedStatement ps = con.prepareStatement(dueSql)) {
                ps.setInt(1, residentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        double due = rs.getDouble(1);
                        maintenanceVal.setText("₹" + String.format("%.0f", due));
                    }
                }
            }

            statusLabel.setText("Updated just now");
        } catch (Exception ex) {
            statusLabel.setText("Update failed");
            LoginFrame.showDbError(ex);
        }
    }
}
