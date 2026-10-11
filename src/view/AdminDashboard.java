package view;

import database.DBConnection;
import util.CodeGenerator;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AdminDashboard extends JFrame {
    private final int adminId;
    private final String adminName;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainContainer = new JPanel(cardLayout);
    private String currentView = "OVERVIEW";

    // Theme Colors matching main app
    private static final Color COFFEE = new Color(55, 36, 27);
    private static final Color CREAM = new Color(250, 245, 235);
    private static final Color AMBER = new Color(190, 124, 58);
    private static final Color LINE = new Color(229, 216, 199);

    // Overview Components
    private final JLabel totalResidentsLbl = new JLabel("0");
    private final JLabel totalWorkersLbl = new JLabel("0");
    private final JLabel activeComplaintsLbl = new JLabel("0");
    private final JLabel totalDuesLbl = new JLabel("₹0");
    private final DefaultTableModel overviewComplaintsModel = new DefaultTableModel(
            new String[] { "ID", "Resident", "Title", "Category", "Status" }, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final DefaultTableModel overviewPaymentsModel = new DefaultTableModel(
            new String[] { "ID", "Resident", "Bill", "Amount (₹)", "Status" }, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };

    // Complaints Components
    private JLabel totalCompStat, pendingCompStat, inProgCompStat, completedCompStat;
    private final DefaultTableModel complaintsModel = new DefaultTableModel(
            new String[] { "ID", "Resident", "Apt", "Title", "Category", "Worker", "Due Date", "Status", "Deadline" }, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable complaintsTable = new JTable(complaintsModel);
    private final JTextArea complaintDescArea = new JTextArea();
    private final JLabel complaintDetailsTitle = new JLabel("Select a complaint from table to view details");
    private JComboBox<WorkerItem> workerDropdown;

    // Payments / Billing Components
    private JLabel totalBilledStat, totalPaidStat, totalPendingStat, totalOverdueStat;
    private final DefaultTableModel paymentsModel = new DefaultTableModel(
            new String[] { "ID", "Resident", "Apt", "Bill Title / Month", "Amount (₹)", "Due Date", "Paid Date", "Method", "Txn ID", "Status" }, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable paymentsTable = new JTable(paymentsModel);

    public static class WorkerItem {
        public final int id;
        public final String name;
        public final String type;

        public WorkerItem(int id, String name, String type) {
            this.id = id;
            this.name = name;
            this.type = type;
        }

        @Override
        public String toString() {
            return name + " (" + type + ") [#" + id + "]";
        }
    }

    public static class ResidentItem {
        public final int id;
        public final String name;
        public final String apt;

        public ResidentItem(int id, String name, String apt) {
            this.id = id;
            this.name = name;
            this.apt = apt;
        }

        @Override
        public String toString() {
            return apt + " - " + name + " (ID: #" + id + ")";
        }
    }

    public AdminDashboard(int id, String name) {
        this.adminId = id;
        this.adminName = name;

        setTitle("A-partment Helper | Society Administrator");
        setSize(1180, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CREAM);
        setContentPane(root);

        // Sidebar Navigation
        JPanel sidebar = new JPanel(null);
        sidebar.setPreferredSize(new Dimension(220, 760));
        sidebar.setBackground(COFFEE);
        root.add(sidebar, BorderLayout.WEST);

        JLabel logo = new JLabel("A-partment");
        logo.setBounds(20, 25, 180, 30);
        logo.setForeground(CREAM);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        sidebar.add(logo);

        JLabel logoSub = new JLabel("Admin Portal");
        logoSub.setBounds(20, 55, 180, 20);
        logoSub.setForeground(new Color(185, 155, 130));
        logoSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sidebar.add(logoSub);

        JButton overviewBtn = createSidebarButton("Overview", getNavIcon("OVERVIEW"));
        overviewBtn.setBounds(15, 100, 190, 42);
        overviewBtn.addActionListener(e -> showView("OVERVIEW"));
        sidebar.add(overviewBtn);

        JButton complaintsBtn = createSidebarButton("Complaints", getNavIcon("COMPLAINTS"));
        complaintsBtn.setBounds(15, 152, 190, 42);
        complaintsBtn.addActionListener(e -> showView("COMPLAINTS"));
        sidebar.add(complaintsBtn);

        JButton billingBtn = createSidebarButton("Billing & Dues", getNavIcon("PAYMENTS"));
        billingBtn.setBounds(15, 204, 190, 42);
        billingBtn.addActionListener(e -> showView("BILLING"));
        sidebar.add(billingBtn);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBounds(15, 650, 190, 40);
        logoutBtn.setIcon(getNavIcon("LOGOUT"));
        logoutBtn.setIconTextGap(14);
        logoutBtn.setMargin(new Insets(0, 16, 0, 10));
        logoutBtn.setHorizontalAlignment(SwingConstants.LEFT);
        logoutBtn.setBackground(new Color(95, 55, 45));
        logoutBtn.setForeground(CREAM);
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorder(BorderFactory.createLineBorder(new Color(135, 75, 60), 1));
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            new WelcomeFrame();
            dispose();
        });
        sidebar.add(logoutBtn);

        // Add Views
        mainContainer.setOpaque(false);
        mainContainer.add(createOverviewPanel(), "OVERVIEW");
        mainContainer.add(createComplaintsPanel(), "COMPLAINTS");
        mainContainer.add(createBillingPanel(), "BILLING");
        root.add(mainContainer, BorderLayout.CENTER);

        showView("OVERVIEW");
        setVisible(true);
    }

    private void showView(String viewName) {
        currentView = viewName;
        cardLayout.show(mainContainer, viewName);

        if ("OVERVIEW".equals(viewName)) {
            loadOverviewData();
        } else if ("COMPLAINTS".equals(viewName)) {
            loadComplaintsData();
        } else if ("BILLING".equals(viewName)) {
            loadBillingData();
        }
    }

    private JPanel createTopBar(String title) {
        JPanel header = new JPanel(null);
        header.setBounds(0, 0, 960, 65);
        header.setBackground(COFFEE);

        JLabel headerTitle = new JLabel(title);
        headerTitle.setBounds(30, 16, 450, 32);
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerTitle.setForeground(CREAM);
        header.add(headerTitle);

        JLabel subHeader = new JLabel("Logged in: " + adminName + " (Admin #" + adminId + ")");
        subHeader.setBounds(500, 22, 425, 20);
        subHeader.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subHeader.setForeground(new Color(220, 195, 165));
        subHeader.setHorizontalAlignment(SwingConstants.RIGHT);
        header.add(subHeader);

        return header;
    }

    // ==========================================
    // 1. OVERVIEW VIEW
    // ==========================================
    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(CREAM);
        panel.add(createTopBar("Society Management Overview"));

        // 4 Summary Metric Cards
        JPanel c1 = createMetricCard("TOTAL RESIDENTS", totalResidentsLbl, 30, 80, 215, 85);
        JPanel c2 = createMetricCard("ACTIVE WORKERS", totalWorkersLbl, 260, 80, 215, 85);
        JPanel c3 = createMetricCard("OPEN COMPLAINTS", activeComplaintsLbl, 490, 80, 215, 85);
        JPanel c4 = createMetricCard("SOCIETY UNPAID DUES", totalDuesLbl, 720, 80, 210, 85);
        panel.add(c1);
        panel.add(c2);
        panel.add(c3);
        panel.add(c4);

        // Recent Complaints Preview Box
        JPanel compBox = new JPanel(null);
        compBox.setBounds(30, 185, 445, 510);
        compBox.setBackground(Color.WHITE);
        compBox.setBorder(BorderFactory.createLineBorder(LINE, 1));
        panel.add(compBox);

        JLabel compTitle = new JLabel("Recent Complaints");
        compTitle.setBounds(20, 15, 250, 25);
        compTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        compTitle.setForeground(COFFEE);
        compBox.add(compTitle);

        JButton viewAllComp = new JButton("Manage All →");
        viewAllComp.setBounds(300, 14, 125, 28);
        viewAllComp.setBackground(new Color(242, 231, 215));
        viewAllComp.setForeground(COFFEE);
        viewAllComp.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewAllComp.setBorder(BorderFactory.createLineBorder(LINE, 1));
        viewAllComp.setFocusPainted(false);
        viewAllComp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewAllComp.addActionListener(e -> showView("COMPLAINTS"));
        compBox.add(viewAllComp);

        JTable ovCompTable = new JTable(overviewComplaintsModel);
        styleTable(ovCompTable);
        JScrollPane ovCompScroll = new JScrollPane(ovCompTable);
        ovCompScroll.setBounds(20, 55, 405, 435);
        ovCompScroll.setBorder(BorderFactory.createLineBorder(LINE, 1));
        compBox.add(ovCompScroll);

        // Recent Payments Preview Box
        JPanel payBox = new JPanel(null);
        payBox.setBounds(495, 185, 435, 510);
        payBox.setBackground(Color.WHITE);
        payBox.setBorder(BorderFactory.createLineBorder(LINE, 1));
        panel.add(payBox);

        JLabel payTitle = new JLabel("Recent Billing & Dues");
        payTitle.setBounds(20, 15, 250, 25);
        payTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        payTitle.setForeground(COFFEE);
        payBox.add(payTitle);

        JButton viewAllPay = new JButton("Manage All →");
        viewAllPay.setBounds(290, 14, 125, 28);
        viewAllPay.setBackground(new Color(242, 231, 215));
        viewAllPay.setForeground(COFFEE);
        viewAllPay.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewAllPay.setBorder(BorderFactory.createLineBorder(LINE, 1));
        viewAllPay.setFocusPainted(false);
        viewAllPay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewAllPay.addActionListener(e -> showView("BILLING"));
        payBox.add(viewAllPay);

        JTable ovPayTable = new JTable(overviewPaymentsModel);
        styleTable(ovPayTable);
        JScrollPane ovPayScroll = new JScrollPane(ovPayTable);
        ovPayScroll.setBounds(20, 55, 395, 435);
        ovPayScroll.setBorder(BorderFactory.createLineBorder(LINE, 1));
        payBox.add(ovPayScroll);

        return panel;
    }

    // ==========================================
    // 2. COMPLAINTS VIEW
    // ==========================================
    private JPanel createComplaintsPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(CREAM);
        panel.add(createTopBar("Maintenance Complaints & Worker Assignment"));

        // 4 Metric cards
        JPanel c1 = createMetricCard("TOTAL COMPLAINTS", totalCompStat = new JLabel("0"), 30, 80, 215, 85);
        JPanel c2 = createMetricCard("PENDING / UNASSIGNED", pendingCompStat = new JLabel("0"), 260, 80, 215, 85);
        JPanel c3 = createMetricCard("IN PROGRESS", inProgCompStat = new JLabel("0"), 490, 80, 215, 85);
        JPanel c4 = createMetricCard("RESOLVED / COMPLETED", completedCompStat = new JLabel("0"), 720, 80, 210, 85);
        panel.add(c1);
        panel.add(c2);
        panel.add(c3);
        panel.add(c4);

        // Control & Table Panel
        JPanel tablePanel = new JPanel(null);
        tablePanel.setBounds(30, 180, 900, 520);
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createLineBorder(LINE, 1));
        panel.add(tablePanel);

        JLabel tblTitle = new JLabel("All Maintenance Complaints");
        tblTitle.setBounds(20, 15, 320, 25);
        tblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tblTitle.setForeground(COFFEE);
        tablePanel.add(tblTitle);

        workerDropdown = new JComboBox<>();
        workerDropdown.setBounds(440, 12, 210, 32);
        workerDropdown.setBackground(Color.WHITE);
        workerDropdown.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablePanel.add(workerDropdown);

        JButton assignManualBtn = new JButton("Assign Selected");
        assignManualBtn.setBounds(660, 12, 130, 32);
        assignManualBtn.setBackground(new Color(242, 231, 215));
        assignManualBtn.setForeground(COFFEE);
        assignManualBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        assignManualBtn.setBorder(BorderFactory.createLineBorder(LINE, 1));
        assignManualBtn.setFocusPainted(false);
        assignManualBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        assignManualBtn.addActionListener(e -> assignSelectedComplaintManual());
        tablePanel.add(assignManualBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBounds(800, 12, 80, 32);
        refreshBtn.setBackground(new Color(242, 231, 215));
        refreshBtn.setForeground(COFFEE);
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setBorder(BorderFactory.createLineBorder(LINE, 1));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> loadComplaintsData());
        tablePanel.add(refreshBtn);

        // Complaints Table
        complaintsTable.setRowHeight(28);
        complaintsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        complaintsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        complaintsTable.setShowVerticalLines(false);
        complaintsTable.setGridColor(new Color(242, 235, 226));
        complaintsTable.setSelectionBackground(new Color(242, 225, 201));
        complaintsTable.setSelectionForeground(COFFEE);
        complaintsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        complaintsTable.getTableHeader().setBackground(new Color(239, 226, 209));
        complaintsTable.getTableHeader().setForeground(COFFEE);

        complaintsTable.getColumnModel().getColumn(0).setPreferredWidth(35);
        complaintsTable.getColumnModel().getColumn(1).setPreferredWidth(105);
        complaintsTable.getColumnModel().getColumn(2).setPreferredWidth(55);
        complaintsTable.getColumnModel().getColumn(3).setPreferredWidth(145);
        complaintsTable.getColumnModel().getColumn(4).setPreferredWidth(75);
        complaintsTable.getColumnModel().getColumn(5).setPreferredWidth(95);
        complaintsTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        complaintsTable.getColumnModel().getColumn(7).setPreferredWidth(85);
        complaintsTable.getColumnModel().getColumn(8).setPreferredWidth(85);

        complaintsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                Object deadlineVal = table.getValueAt(row, 8);
                Object statusVal = table.getValueAt(row, 7);

                if (!isSelected) {
                    if (deadlineVal != null && deadlineVal.toString().contains("OVERDUE")) {
                        c.setForeground(new Color(195, 35, 35));
                        if (col == 7 || col == 8) c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if (statusVal != null && (statusVal.toString().equalsIgnoreCase("RESOLVED") || statusVal.toString().equalsIgnoreCase("COMPLETED"))) {
                        c.setForeground(new Color(24, 134, 75));
                        if (col == 7 || col == 8) c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if (statusVal != null && statusVal.toString().toUpperCase().contains("IN PROGRESS")) {
                        c.setForeground(new Color(205, 115, 25));
                        if (col == 7 || col == 8) c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setForeground(COFFEE);
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    }
                }
                return c;
            }
        });

        // Detail selection listener
        complaintsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedComplaintDescription();
            }
        });

        JScrollPane scrollPane = new JScrollPane(complaintsTable);
        scrollPane.setBounds(20, 55, 860, 310);
        scrollPane.setBorder(BorderFactory.createLineBorder(LINE, 1));
        tablePanel.add(scrollPane);

        // Selected Complaint Detail Box at bottom
        JPanel detailBox = new JPanel(null);
        detailBox.setBounds(20, 375, 860, 130);
        detailBox.setBackground(new Color(253, 250, 246));
        detailBox.setBorder(BorderFactory.createLineBorder(LINE, 1));
        tablePanel.add(detailBox);

        complaintDetailsTitle.setBounds(15, 8, 830, 20);
        complaintDetailsTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        complaintDetailsTitle.setForeground(COFFEE);
        detailBox.add(complaintDetailsTitle);

        complaintDescArea.setEditable(false);
        complaintDescArea.setLineWrap(true);
        complaintDescArea.setWrapStyleWord(true);
        complaintDescArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        complaintDescArea.setBackground(Color.WHITE);
        JScrollPane descScroll = new JScrollPane(complaintDescArea);
        descScroll.setBounds(15, 32, 830, 85);
        descScroll.setBorder(BorderFactory.createLineBorder(LINE, 1));
        detailBox.add(descScroll);

        return panel;
    }

    // ==========================================
    // 3. BILLING VIEW
    // ==========================================
    private JPanel createBillingPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(CREAM);
        panel.add(createTopBar("Society Billing Ledger & Dues Management"));

        // 4 Metric cards
        JPanel c1 = createMetricCard("TOTAL BILLED", totalBilledStat = new JLabel("₹0"), 30, 80, 215, 85);
        JPanel c2 = createMetricCard("TOTAL COLLECTED", totalPaidStat = new JLabel("₹0"), 260, 80, 215, 85);
        JPanel c3 = createMetricCard("TOTAL PENDING", totalPendingStat = new JLabel("₹0"), 490, 80, 215, 85);
        JPanel c4 = createMetricCard("OVERDUE AMOUNT", totalOverdueStat = new JLabel("₹0"), 720, 80, 210, 85);
        panel.add(c1);
        panel.add(c2);
        panel.add(c3);
        panel.add(c4);

        // Table Panel
        JPanel tablePanel = new JPanel(null);
        tablePanel.setBounds(30, 180, 900, 520);
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createLineBorder(LINE, 1));
        panel.add(tablePanel);

        JLabel tblTitle = new JLabel("Resident Payment Records & Ledger");
        tblTitle.setBounds(20, 15, 350, 25);
        tblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tblTitle.setForeground(COFFEE);
        tablePanel.add(tblTitle);

        JButton createBillBtn = new JButton("+ Generate Custom Bill");
        createBillBtn.setBounds(560, 12, 210, 34);
        createBillBtn.setBackground(new Color(40, 135, 75));
        createBillBtn.setForeground(Color.WHITE);
        createBillBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        createBillBtn.setFocusPainted(false);
        createBillBtn.setBorder(BorderFactory.createLineBorder(new Color(30, 110, 60), 1));
        createBillBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        createBillBtn.addActionListener(e -> showGenerateBillDialog());
        tablePanel.add(createBillBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBounds(785, 12, 95, 34);
        refreshBtn.setBackground(new Color(242, 231, 215));
        refreshBtn.setForeground(COFFEE);
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setBorder(BorderFactory.createLineBorder(LINE, 1));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> loadBillingData());
        tablePanel.add(refreshBtn);

        paymentsTable.setRowHeight(28);
        paymentsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        paymentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        paymentsTable.setShowVerticalLines(false);
        paymentsTable.setGridColor(new Color(242, 235, 226));
        paymentsTable.setSelectionBackground(new Color(242, 225, 201));
        paymentsTable.setSelectionForeground(COFFEE);
        paymentsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        paymentsTable.getTableHeader().setBackground(new Color(239, 226, 209));
        paymentsTable.getTableHeader().setForeground(COFFEE);

        paymentsTable.getColumnModel().getColumn(0).setPreferredWidth(35);
        paymentsTable.getColumnModel().getColumn(1).setPreferredWidth(110);
        paymentsTable.getColumnModel().getColumn(2).setPreferredWidth(55);
        paymentsTable.getColumnModel().getColumn(3).setPreferredWidth(155);
        paymentsTable.getColumnModel().getColumn(4).setPreferredWidth(85);
        paymentsTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        paymentsTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        paymentsTable.getColumnModel().getColumn(7).setPreferredWidth(75);
        paymentsTable.getColumnModel().getColumn(8).setPreferredWidth(110);
        paymentsTable.getColumnModel().getColumn(9).setPreferredWidth(75);

        paymentsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                Object statusVal = table.getValueAt(row, 9);

                if (!isSelected) {
                    if (statusVal != null && statusVal.toString().contains("OVERDUE")) {
                        c.setForeground(new Color(195, 35, 35));
                        if (col == 9) c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if (statusVal != null && statusVal.toString().contains("PAID")) {
                        c.setForeground(new Color(24, 134, 75));
                        if (col == 9) c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setForeground(COFFEE);
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(paymentsTable);
        scrollPane.setBounds(20, 58, 860, 440);
        scrollPane.setBorder(BorderFactory.createLineBorder(LINE, 1));
        tablePanel.add(scrollPane);

        return panel;
    }

    // ==========================================
    // AUTO-ASSIGN & BILLING LOGIC
    // ==========================================

    /**
     * Finds the best worker for a complaint based on the user's specific rule:
     * 1. Look for workers with 0 active tasks (unassigned).
     * 2. ONLY look for assigned workers if NO workers are unassigned.
     */
    public static Integer findWorkerForComplaint(Connection con, String category) throws SQLException {
        // Step 1: Look for an unassigned worker (0 active complaints in PENDING, ASSIGNED, IN_PROGRESS)
        String unassignedSql =
            "SELECT w.worker_id " +
            "FROM workers w " +
            "WHERE NOT EXISTS (" +
            "    SELECT 1 FROM complaints c " +
            "    WHERE c.worker_id = w.worker_id " +
            "    AND c.status IN ('PENDING', 'ASSIGNED', 'IN_PROGRESS', 'IN PROGRESS')" +
            ") " +
            "ORDER BY CASE WHEN lower(w.worker_type) = lower(?) THEN 0 ELSE 1 END, w.worker_id ASC " +
            "LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(unassignedSql)) {
            ps.setString(1, category != null ? category : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("worker_id"); // Found unassigned worker!
                }
            }
        }

        // Step 2: ONLY IF NO workers are unassigned, look among already-assigned workers (least busy)
        String busySql =
            "SELECT w.worker_id, COUNT(c.complaint_id) AS active_cnt " +
            "FROM workers w " +
            "LEFT JOIN complaints c ON w.worker_id = c.worker_id " +
            "    AND c.status IN ('PENDING', 'ASSIGNED', 'IN_PROGRESS', 'IN PROGRESS') " +
            "GROUP BY w.worker_id, w.worker_type " +
            "ORDER BY active_cnt ASC, CASE WHEN lower(w.worker_type) = lower(?) THEN 0 ELSE 1 END, w.worker_id ASC " +
            "LIMIT 1";

        try (PreparedStatement ps = con.prepareStatement(busySql)) {
            ps.setString(1, category != null ? category : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("worker_id");
                }
            }
        }

        return null;
    }

    private void assignSelectedComplaintManual() {
        int selectedRow = complaintsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a complaint row from the table first.", "Select Complaint", JOptionPane.WARNING_MESSAGE);
            return;
        }

        WorkerItem item = (WorkerItem) workerDropdown.getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "No worker selected from dropdown.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int complaintId = (int) complaintsModel.getValueAt(selectedRow, 0);

        String sql = "UPDATE complaints SET worker_id = ?, status = 'ASSIGNED' WHERE complaint_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, item.id);
            ps.setInt(2, complaintId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Complaint #" + complaintId + " assigned to " + item.name + "!");
                loadComplaintsData();
            }
        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
        }
    }

    private void showSelectedComplaintDescription() {
        int row = complaintsTable.getSelectedRow();
        if (row < 0) {
            complaintDetailsTitle.setText("Select a complaint from table to view details");
            complaintDescArea.setText("");
            return;
        }

        int id = (int) complaintsModel.getValueAt(row, 0);
        String res = (String) complaintsModel.getValueAt(row, 1);
        String apt = (String) complaintsModel.getValueAt(row, 2);
        String title = (String) complaintsModel.getValueAt(row, 3);
        String worker = (String) complaintsModel.getValueAt(row, 5);

        complaintDetailsTitle.setText("Complaint #" + id + " — " + title + " | Resident: " + res + " (" + apt + ") | Worker: " + worker);

        String sql = "SELECT description FROM complaints WHERE complaint_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String desc = rs.getString("description");
                    complaintDescArea.setText(desc != null && !desc.isBlank() ? desc : "No description provided.");
                }
            }
        } catch (Exception ex) {
            complaintDescArea.setText("Error loading description: " + ex.getMessage());
        }
    }

    private void showGenerateBillDialog() {
        List<ResidentItem> residents = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT resident_id, full_name, apartment_no FROM residents ORDER BY apartment_no")) {
            while (rs.next()) {
                residents.add(new ResidentItem(rs.getInt("resident_id"), rs.getString("full_name"), rs.getString("apartment_no")));
            }
        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
            return;
        }

        if (residents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No residents found in database.");
            return;
        }

        JComboBox<ResidentItem> resBox = new JComboBox<>(residents.toArray(new ResidentItem[0]));
        JTextField titleField = new JTextField();
        JTextField amountField = new JTextField();

        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        cal.add(Calendar.DAY_OF_MONTH, 15);
        SpinnerDateModel dateModel = new SpinnerDateModel(cal.getTime(), today, null, Calendar.DAY_OF_MONTH);
        JSpinner dueSpinner = new JSpinner(dateModel);
        dueSpinner.setEditor(new JSpinner.DateEditor(dueSpinner, "yyyy-MM-dd"));

        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.add(new JLabel("Select Resident:"));
        form.add(resBox);
        form.add(new JLabel("Bill Title / Purpose (e.g. Clubhouse Booking, Water Surcharge):"));
        form.add(titleField);
        form.add(new JLabel("Amount (₹):"));
        form.add(amountField);
        form.add(new JLabel("Due Date:"));
        form.add(dueSpinner);

        int result = JOptionPane.showConfirmDialog(this, form, "Generate Custom Bill for Resident", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        ResidentItem selectedRes = (ResidentItem) resBox.getSelectedItem();
        String title = titleField.getText().trim();
        String amtStr = amountField.getText().trim();
        Date dueDate = (Date) dueSpinner.getValue();

        if (selectedRes == null || title.isEmpty() || amtStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amtStr);
            if (amount <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive bill amount.", "Invalid Amount", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dueDateStr = sdf.format(dueDate);

        String insertSql = "INSERT INTO payments (resident_id, month_year, amount, due_date, status, payment_type) " +
                           "VALUES (?, ?, ?, ?::DATE, 'PENDING', 'CUSTOM')";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(insertSql)) {

            ps.setInt(1, selectedRes.id);
            ps.setString(2, title);
            ps.setDouble(3, amount);
            ps.setString(4, dueDateStr);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this,
                        "Bill successfully generated and assigned to " + selectedRes.name + " (" + selectedRes.apt + ")!\n" +
                        "Amount: ₹" + String.format("%.2f", amount) + "\nDue Date: " + dueDateStr,
                        "Bill Created", JOptionPane.INFORMATION_MESSAGE);
                loadBillingData();
            }
        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
        }
    }

    // ==========================================
    // DATA LOADERS
    // ==========================================
    private void loadOverviewData() {
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;

            // Residents count
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM residents")) {
                if (rs.next()) totalResidentsLbl.setText(String.valueOf(rs.getInt(1)));
            }

            // Workers count
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM workers")) {
                if (rs.next()) totalWorkersLbl.setText(String.valueOf(rs.getInt(1)));
            }

            // Open complaints count
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM complaints WHERE status != 'COMPLETED'")) {
                if (rs.next()) activeComplaintsLbl.setText(String.valueOf(rs.getInt(1)));
            }

            // Unpaid dues sum
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE status != 'PAID'")) {
                if (rs.next()) totalDuesLbl.setText("₹" + String.format("%.0f", rs.getDouble(1)));
            }

            // Overview complaints preview
            overviewComplaintsModel.setRowCount(0);
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT c.complaint_id, r.full_name, c.title, c.category, c.status " +
                                                "FROM complaints c JOIN residents r ON c.resident_id = r.resident_id " +
                                                "ORDER BY c.complaint_id DESC LIMIT 15")) {
                while (rs.next()) {
                    String stText = rs.getString("status");
                    if (stText != null) stText = stText.replace("_", " ");
                    overviewComplaintsModel.addRow(new Object[]{
                            rs.getInt("complaint_id"),
                            rs.getString("full_name"),
                            rs.getString("title"),
                            rs.getString("category"),
                            stText
                    });
                }
            }

            // Overview payments preview
            overviewPaymentsModel.setRowCount(0);
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT p.payment_id, r.full_name, p.month_year, p.amount, p.status " +
                                                "FROM payments p JOIN residents r ON p.resident_id = r.resident_id " +
                                                "ORDER BY p.payment_id DESC LIMIT 15")) {
                while (rs.next()) {
                    overviewPaymentsModel.addRow(new Object[]{
                            rs.getInt("payment_id"),
                            rs.getString("full_name"),
                            rs.getString("month_year"),
                            String.format("%.2f", rs.getDouble("amount")),
                            rs.getString("status")
                    });
                }
            }
        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
        }
    }

    private void loadComplaintsData() {
        complaintsModel.setRowCount(0);

        // Load workers into dropdown
        if (workerDropdown != null) {
            workerDropdown.removeAllItems();
            try (Connection con = DBConnection.getConnection();
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT worker_id, full_name, worker_type FROM workers ORDER BY full_name")) {
                while (rs.next()) {
                    workerDropdown.addItem(new WorkerItem(rs.getInt("worker_id"), rs.getString("full_name"), rs.getString("worker_type")));
                }
            } catch (Exception ignored) {}
        }

        String sql = "SELECT c.complaint_id, r.full_name, r.apartment_no, c.title, c.category, " +
                     "COALESCE(w.full_name, 'Unassigned') AS worker_name, c.due_date, c.status " +
                     "FROM complaints c " +
                     "JOIN residents r ON c.resident_id = r.resident_id " +
                     "LEFT JOIN workers w ON c.worker_id = w.worker_id " +
                     "ORDER BY c.complaint_id DESC";

        int total = 0, pending = 0, inProg = 0, completed = 0;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            LocalDate today = LocalDate.now();

            while (rs.next()) {
                total++;
                int id = rs.getInt("complaint_id");
                String resName = rs.getString("full_name");
                String apt = rs.getString("apartment_no");
                String title = rs.getString("title");
                String cat = rs.getString("category");
                String worker = rs.getString("worker_name");
                java.sql.Date due = rs.getDate("due_date");
                String rawStatus = rs.getString("status");
                String displayStatus = rawStatus != null ? rawStatus.replace("_", " ") : "PENDING";

                String deadline = "On Schedule";
                if ("COMPLETED".equalsIgnoreCase(rawStatus) || "RESOLVED".equalsIgnoreCase(rawStatus)) {
                    completed++;
                    deadline = "Completed";
                } else if ("IN_PROGRESS".equalsIgnoreCase(rawStatus) || "IN PROGRESS".equalsIgnoreCase(rawStatus)) {
                    inProg++;
                } else {
                    pending++;
                }

                if (due != null && !"Completed".equals(deadline) && today.isAfter(due.toLocalDate())) {
                    deadline = "⚠️ OVERDUE";
                }

                complaintsModel.addRow(new Object[]{
                        id, resName, apt, title, cat, worker,
                        due != null ? due.toString() : "Not Set",
                        displayStatus, deadline
                });
            }

            if (totalCompStat != null) totalCompStat.setText(String.valueOf(total));
            if (pendingCompStat != null) pendingCompStat.setText(String.valueOf(pending));
            if (inProgCompStat != null) inProgCompStat.setText(String.valueOf(inProg));
            if (completedCompStat != null) completedCompStat.setText(String.valueOf(completed));

        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
        }
    }

    private void loadBillingData() {
        paymentsModel.setRowCount(0);

        String sql = "SELECT p.payment_id, r.full_name, r.apartment_no, p.month_year, p.amount, p.due_date, " +
                     "p.payment_date, p.payment_method, p.transaction_id, p.status " +
                     "FROM payments p " +
                     "JOIN residents r ON p.resident_id = r.resident_id " +
                     "ORDER BY p.payment_id DESC";

        double totalBilled = 0, totalPaid = 0, totalPending = 0, totalOverdue = 0;
        LocalDate today = LocalDate.now();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("payment_id");
                String resName = rs.getString("full_name");
                String apt = rs.getString("apartment_no");
                String title = rs.getString("month_year");
                double amt = rs.getDouble("amount");
                java.sql.Date due = rs.getDate("due_date");
                java.sql.Date paidDate = rs.getDate("payment_date");
                String method = rs.getString("payment_method");
                String txn = rs.getString("transaction_id");
                String status = rs.getString("status");

                totalBilled += amt;
                if ("PAID".equalsIgnoreCase(status)) {
                    totalPaid += amt;
                } else {
                    totalPending += amt;
                    if (due != null && today.isAfter(due.toLocalDate())) {
                        totalOverdue += amt;
                        status = "⚠️ OVERDUE";
                    }
                }

                paymentsModel.addRow(new Object[]{
                        id, resName, apt, title, String.format("%.2f", amt),
                        due != null ? due.toString() : "-",
                        paidDate != null ? paidDate.toString() : "-",
                        method != null ? method : "-",
                        txn != null ? txn : "-",
                        status != null ? status : "PENDING"
                });
            }

            if (totalBilledStat != null) totalBilledStat.setText("₹" + String.format("%.0f", totalBilled));
            if (totalPaidStat != null) totalPaidStat.setText("₹" + String.format("%.0f", totalPaid));
            if (totalPendingStat != null) totalPendingStat.setText("₹" + String.format("%.0f", totalPending));
            if (totalOverdueStat != null) totalOverdueStat.setText("₹" + String.format("%.0f", totalOverdue));

        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
        }
    }

    // ==========================================
    // HELPER UI METHODS
    // ==========================================
    private JPanel createMetricCard(String title, JLabel valueLabel, int x, int y, int width, int height) {
        JPanel card = new JPanel(null);
        card.setBounds(x, y, width, height);
        card.setBackground(new Color(242, 231, 215));
        card.setBorder(BorderFactory.createLineBorder(new Color(233, 218, 197), 1));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setBounds(16, 12, width - 24, 20);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(new Color(115, 82, 56));
        card.add(titleLbl);

        valueLabel.setBounds(16, 36, width - 24, 38);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(COFFEE);
        card.add(valueLabel);

        return card;
    }

    private JButton createSidebarButton(String text, Icon icon) {
        JButton b = new JButton(text);
        b.setIcon(icon);
        b.setIconTextGap(14);
        b.setMargin(new Insets(0, 16, 0, 10));
        b.setBackground(new Color(68, 46, 35));
        b.setForeground(CREAM);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createLineBorder(new Color(98, 70, 54), 1));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static Icon getNavIcon(String type) {
        return new Icon() {
            public int getIconWidth() { return 18; }
            public int getIconHeight() { return 18; }
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CREAM);

                if ("OVERVIEW".equals(type)) {
                    g2.fillRoundRect(x, y, 7, 7, 2, 2);
                    g2.fillRoundRect(x + 9, y, 7, 7, 2, 2);
                    g2.fillRoundRect(x, y + 9, 7, 7, 2, 2);
                    g2.fillRoundRect(x + 9, y + 9, 7, 7, 2, 2);
                } else if ("COMPLAINTS".equals(type)) {
                    g2.setStroke(new BasicStroke(1.8f));
                    g2.drawRoundRect(x + 2, y + 3, 14, 13, 3, 3);
                    g2.fillRect(x + 6, y + 1, 6, 3);
                    g2.drawLine(x + 5, y + 7, x + 13, y + 7);
                    g2.drawLine(x + 5, y + 10, x + 13, y + 10);
                } else if ("PAYMENTS".equals(type)) {
                    g2.setStroke(new BasicStroke(1.8f));
                    g2.drawRoundRect(x + 1, y + 2, 16, 13, 3, 3);
                    g2.fillRect(x + 1, y + 5, 16, 3);
                    g2.fillRect(x + 3, y + 10, 4, 3);
                } else if ("LOGOUT".equals(type)) {
                    g2.setStroke(new BasicStroke(1.8f));
                    g2.drawLine(x + 9, y + 2, x + 3, y + 2);
                    g2.drawLine(x + 3, y + 2, x + 3, y + 16);
                    g2.drawLine(x + 3, y + 16, x + 9, y + 16);
                    g2.drawLine(x + 7, y + 9, x + 15, y + 9);
                    g2.drawLine(x + 12, y + 6, x + 15, y + 9);
                    g2.drawLine(x + 12, y + 12, x + 15, y + 9);
                }
                g2.dispose();
            }
        };
    }

    private void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(242, 235, 226));
        table.setSelectionBackground(new Color(242, 225, 201));
        table.setSelectionForeground(COFFEE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(239, 226, 209));
        table.getTableHeader().setForeground(COFFEE);
    }
}
