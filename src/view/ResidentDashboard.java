package view;

import database.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ResidentDashboard extends JFrame {
    private final int residentId;
    private final String residentName;

    // CardLayout to display different views within the SAME window
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainContainer = new JPanel(cardLayout);
    private String currentView = "OVERVIEW";

    // Overview UI Components
    private final JLabel parkingVal = new JLabel("—");
    private final JLabel visitorsVal = new JLabel("0");
    private final JLabel maintenanceVal = new JLabel("₹0");
    private final JLabel overviewStatus = new JLabel(" ");
    private final DefaultTableModel visitorModel = new DefaultTableModel(
            new String[] { "Visitor", "Phone", "Parking", "Status", "Valid Until" }, 0) {
        public boolean isCellEditable(int row, int col) { return false; }
    };

    // Complaints UI Components
    private final DefaultTableModel complaintsModel = new DefaultTableModel(
            new String[] { "ID", "Title", "Category", "Status", "Created" }, 0) {
        public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable complaintsTable = new JTable(complaintsModel);

    // Payments UI Components
    private final DefaultTableModel paymentsModel = new DefaultTableModel(
            new String[] { "ID", "Period", "Amount (₹)", "Due Date", "Status", "Paid On" }, 0) {
        public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable paymentsTable = new JTable(paymentsModel);

    // Colors
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

        // Sidebar buttons switch views inside the main container
        sidebar.add(createMenuItem("Overview", () -> showView("OVERVIEW")));
        sidebar.add(createMenuItem("Complaints", () -> showView("COMPLAINTS")));
        sidebar.add(createMenuItem("Payments", () -> showView("PAYMENTS")));
        sidebar.add(createMenuItem("Refresh data", this::refreshCurrentView));

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

        // Add Views into the CardLayout Container
        mainContainer.setOpaque(false);
        mainContainer.add(createOverviewPanel(), "OVERVIEW");
        mainContainer.add(createComplaintsPanel(), "COMPLAINTS");
        mainContainer.add(createPaymentsPanel(), "PAYMENTS");
        root.add(mainContainer, BorderLayout.CENTER);

        // Style the tables
        styleTable(complaintsTable);
        styleTable(paymentsTable);

        // Start on Overview screen
        showView("OVERVIEW");
        setVisible(true);
    }

    // Switches between views inside the same window
    private void showView(String viewName) {
        currentView = viewName;
        cardLayout.show(mainContainer, viewName);

        if ("OVERVIEW".equals(viewName)) {
            loadDashboardData();
        } else if ("COMPLAINTS".equals(viewName)) {
            loadComplaints();
        } else if ("PAYMENTS".equals(viewName)) {
            loadPayments();
        }
    }

    private void refreshCurrentView() {
        showView(currentView);
    }

    // ==========================================
    // 1. OVERVIEW VIEW
    // ==========================================
    private JPanel createOverviewPanel() {
        JPanel content = new JPanel(new BorderLayout(18, 18));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(26, 28, 24, 28));

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

        // Recent Visitors Table
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

        overviewStatus.setForeground(new Color(126, 105, 86));
        tableHeader.add(overviewStatus, BorderLayout.EAST);
        tablePanel.add(tableHeader, BorderLayout.NORTH);

        JTable table = new JTable(visitorModel);
        styleTable(table);
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        JLabel noteLabel = new JLabel("Vehicle visitor parking is assigned automatically when confirmed at the gate.");
        noteLabel.setForeground(new Color(115, 95, 78));
        noteLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        tablePanel.add(noteLabel, BorderLayout.SOUTH);

        content.add(tablePanel, BorderLayout.CENTER);
        return content;
    }

    // ==========================================
    // 2. COMPLAINTS VIEW
    // ==========================================
    private JPanel createComplaintsPanel() {
        JPanel content = new JPanel(new BorderLayout(18, 18));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(26, 28, 24, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Maintenance & Complaints");
        title.setFont(new Font("Serif", Font.BOLD, 29));
        title.setForeground(COFFEE);
        header.add(title, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> loadComplaints());

        JButton newComplaintBtn = LoginFrame.button("+ NEW COMPLAINT");
        newComplaintBtn.addActionListener(e -> createComplaintDialog());

        buttonPanel.add(refreshBtn);
        buttonPanel.add(newComplaintBtn);
        header.add(buttonPanel, BorderLayout.EAST);
        content.add(header, BorderLayout.NORTH);

        // Complaints Table
        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        tablePanel.add(new JScrollPane(complaintsTable), BorderLayout.CENTER);
        content.add(tablePanel, BorderLayout.CENTER);

        return content;
    }

    // ==========================================
    // 3. PAYMENTS VIEW
    // ==========================================
    private JPanel createPaymentsPanel() {
        JPanel content = new JPanel(new BorderLayout(18, 18));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(26, 28, 24, 28));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Maintenance Payments");
        title.setFont(new Font("Serif", Font.BOLD, 29));
        title.setForeground(COFFEE);
        header.add(title, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> loadPayments());

        JButton recordBtn = LoginFrame.button("RECORD PAYMENT");
        recordBtn.addActionListener(e -> recordPaymentDialog());

        buttonPanel.add(refreshBtn);
        buttonPanel.add(recordBtn);
        header.add(buttonPanel, BorderLayout.EAST);
        content.add(header, BorderLayout.NORTH);

        // Payments Table
        JPanel tablePanel = new JPanel(new BorderLayout(0, 10));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        tablePanel.add(new JScrollPane(paymentsTable), BorderLayout.CENTER);
        content.add(tablePanel, BorderLayout.CENTER);

        return content;
    }

    // ==========================================
    // DATABASE LOAD METHODS
    // ==========================================

    // Loads Overview metrics and recent visitors
    public void loadDashboardData() {
        overviewStatus.setText("Updating...");
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

            overviewStatus.setText("Updated just now");
        } catch (Exception ex) {
            overviewStatus.setText("Update failed");
            LoginFrame.showDbError(ex);
        }
    }

    // Loads complaints list
    private void loadComplaints() {
        complaintsModel.setRowCount(0);
        String sql = "SELECT complaint_id, title, category, status, created_at FROM complaints WHERE resident_id = ? ORDER BY complaint_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, residentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    complaintsModel.addRow(new Object[] {
                        rs.getInt("complaint_id"),
                        rs.getString("title"),
                        rs.getString("category"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                    });
                }
            }
        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
        }
    }

    // Opens a popup form to lodge a new complaint
    private void createComplaintDialog() {
        JTextField titleField = new JTextField();
        JTextField descField = new JTextField();
        JComboBox<String> categoryBox = new JComboBox<>(
                new String[] { "Plumbing", "Electrical", "Cleaning", "Security", "Lift", "Other" });

        JPanel form = new JPanel(new GridLayout(0, 1, 5, 5));
        form.add(new JLabel("Short title:"));
        form.add(titleField);
        form.add(new JLabel("Category:"));
        form.add(categoryBox);
        form.add(new JLabel("Description:"));
        form.add(descField);

        int result = JOptionPane.showConfirmDialog(this, form, "Submit New Complaint", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String title = titleField.getText().trim();
        String desc = descField.getText().trim();
        String category = (String) categoryBox.getSelectedItem();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a title.");
            return;
        }

        String sql = "INSERT INTO complaints(resident_id, title, description, category, status) VALUES(?, ?, ?, ?, 'PENDING')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, residentId);
            ps.setString(2, title);
            ps.setString(3, desc);
            ps.setString(4, category);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Complaint submitted successfully.");
            loadComplaints();
        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
        }
    }

    // Loads payments list
    private void loadPayments() {
        paymentsModel.setRowCount(0);
        String sql = "SELECT payment_id, month_year, amount, due_date, status, payment_date " +
                     "FROM payments WHERE resident_id = ? ORDER BY due_date DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, residentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    paymentsModel.addRow(new Object[] {
                        rs.getInt("payment_id"),
                        rs.getString("month_year"),
                        rs.getBigDecimal("amount"),
                        rs.getDate("due_date"),
                        rs.getString("status"),
                        rs.getDate("payment_date")
                    });
                }
            }
        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
        }
    }

    // Records payment for a selected bill
    private void recordPaymentDialog() {
        int selectedRow = paymentsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a payment from the table first.");
            return;
        }

        int paymentId = (int) paymentsModel.getValueAt(selectedRow, 0);
        String currentStatus = String.valueOf(paymentsModel.getValueAt(selectedRow, 4));

        if (!"PENDING".equalsIgnoreCase(currentStatus) && !"OVERDUE".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "This payment is already marked as paid.");
            return;
        }

        String[] methods = { "UPI", "Cash", "Bank transfer", "Card" };
        String method = (String) JOptionPane.showInputDialog(this,
                "Select payment method:", "Record payment",
                JOptionPane.QUESTION_MESSAGE, null, methods, methods[0]);

        if (method == null) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm recording payment for bill #" + paymentId + "?",
                "Confirm payment", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "UPDATE payments SET status = 'PAID', payment_date = CURRENT_DATE, payment_method = ? " +
                     "WHERE payment_id = ? AND resident_id = ? AND status IN ('PENDING', 'OVERDUE')";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, method);
            ps.setInt(2, paymentId);
            ps.setInt(3, residentId);

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(this, "Payment successfully recorded!");
            } else {
                JOptionPane.showMessageDialog(this, "Payment record could not be updated.");
            }
            loadPayments();
            loadDashboardData();
        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
        }
    }

    // ==========================================
    // HELPER UI METHODS
    // ==========================================
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
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(242, 235, 226));
        table.setSelectionBackground(new Color(242, 225, 201));
        table.setSelectionForeground(COFFEE);
        table.getTableHeader().setBackground(new Color(239, 226, 209));
        table.getTableHeader().setForeground(COFFEE);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
    }
}
