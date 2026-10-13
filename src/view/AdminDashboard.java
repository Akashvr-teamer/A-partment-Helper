package view;

import database.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

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
            new String[] { "ID", "Resident", "Apt", "Title", "Category", "Worker", "Status", "Date Logged" }, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable complaintsTable = new JTable(complaintsModel);
    private final JTextArea complaintDescArea = new JTextArea();
    private final JLabel complaintDetailsTitle = new JLabel("Select a complaint from table to view details");
    private JComboBox<WorkerItem> workerDropdown;

    // Payments / Billing Components
    private JLabel totalBilledStat, totalPaidStat, totalPendingStat, totalOverdueStat;
    private final DefaultTableModel paymentsModel = new DefaultTableModel(
            new String[] { "ID", "Resident", "Apt", "Bill Title / Month", "Amount (₹)", "Due Date", "Paid Date", "Method", "Status" }, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable paymentsTable = new JTable(paymentsModel);

    // Directory Components
    private final DefaultTableModel residentsModel = new DefaultTableModel(
            new String[] { "ID", "Full Name", "Apt", "Email", "Phone", "Vehicle No", "Parking Slot", "Joined Date" }, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable residentsTable = new JTable(residentsModel);
    private TableRowSorter<DefaultTableModel> residentsSorter;
    private final JLabel residentsCountSummary = new JLabel("Total Residents: 0");

    private final DefaultTableModel workersModel = new DefaultTableModel(
            new String[] { "ID", "Full Name", "Specialty / Role", "Email", "Phone", "Active Tasks", "Resolved", "Joined Date" }, 0) {
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable workersTable = new JTable(workersModel);
    private TableRowSorter<DefaultTableModel> workersSorter;
    private final JLabel workersCountSummary = new JLabel("Total Workers: 0");
    private JComboBox<String> workerRoleFilterCombo;

    private JButton dirResidentsTabBtn;
    private JButton dirWorkersTabBtn;
    private CardLayout dirCardLayout;
    private JPanel dirCardContainer;

    // Navigation Buttons Map for Active State Highlighting
    private final java.util.Map<String, JButton> navButtons = new java.util.HashMap<>();

    // Registration Form Inputs
    private final JTextField regResNameField = new JTextField();
    private final JTextField regResEmailField = new JTextField();
    private final JTextField regResPhoneField = new JTextField();
    private final JTextField regResAptField = new JTextField();
    private final JPasswordField regResPassField = new JPasswordField();
    private final JTextField regResVehicleField = new JTextField();
    private final JTextField regResParkingField = new JTextField();

    private final JTextField regWorkerNameField = new JTextField();
    private final JTextField regWorkerEmailField = new JTextField();
    private final JTextField regWorkerPhoneField = new JTextField();
    private final JComboBox<String> regWorkerTypeCombo = new JComboBox<>(new String[] {
            "Electrician", "Plumber", "Security", "Cleaning / Housekeeping",
            "Carpenter", "Gardener", "HVAC / AC Repair", "General Maintenance", "Other"
    });
    private final JPasswordField regWorkerPassField = new JPasswordField();

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
        logo.setBounds(20, 22, 180, 28);
        logo.setForeground(CREAM);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        sidebar.add(logo);

        JLabel logoSub = new JLabel("Admin Portal");
        logoSub.setBounds(20, 50, 180, 18);
        logoSub.setForeground(new Color(185, 155, 130));
        logoSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sidebar.add(logoSub);

        JButton overviewBtn = createSidebarButton("Overview", getNavIcon("OVERVIEW"));
        overviewBtn.setBounds(20, 88, 180, 40);
        overviewBtn.addActionListener(e -> showView("OVERVIEW"));
        sidebar.add(overviewBtn);
        navButtons.put("OVERVIEW", overviewBtn);

        JButton complaintsBtn = createSidebarButton("Complaints", getNavIcon("COMPLAINTS"));
        complaintsBtn.setBounds(20, 138, 180, 40);
        complaintsBtn.addActionListener(e -> showView("COMPLAINTS"));
        sidebar.add(complaintsBtn);
        navButtons.put("COMPLAINTS", complaintsBtn);

        JButton billingBtn = createSidebarButton("Billing & Dues", getNavIcon("PAYMENTS"));
        billingBtn.setBounds(20, 188, 180, 40);
        billingBtn.addActionListener(e -> showView("BILLING"));
        sidebar.add(billingBtn);
        navButtons.put("BILLING", billingBtn);

        JButton registerBtn = createSidebarButton("Register User", getNavIcon("REGISTER"));
        registerBtn.setBounds(20, 238, 180, 40);
        registerBtn.addActionListener(e -> showView("REGISTER"));
        sidebar.add(registerBtn);
        navButtons.put("REGISTER", registerBtn);

        JButton directoryBtn = createSidebarButton("Manage Directory", getNavIcon("DIRECTORY"));
        directoryBtn.setBounds(20, 288, 180, 40);
        directoryBtn.addActionListener(e -> showView("DIRECTORY"));
        sidebar.add(directoryBtn);
        navButtons.put("DIRECTORY", directoryBtn);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setBounds(20, 650, 180, 40);
        logoutBtn.setIcon(getNavIcon("LOGOUT"));
        logoutBtn.setIconTextGap(14);
        logoutBtn.setHorizontalAlignment(SwingConstants.LEFT);
        logoutBtn.setBackground(new Color(95, 55, 45));
        logoutBtn.setForeground(CREAM);
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(135, 75, 60), 1),
                BorderFactory.createEmptyBorder(0, 16, 0, 10)
        ));
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
        mainContainer.add(createRegisterPanel(), "REGISTER");
        mainContainer.add(createDirectoryPanel(), "DIRECTORY");
        root.add(mainContainer, BorderLayout.CENTER);

        showView("OVERVIEW");
        setVisible(true);
    }

    private void showView(String viewName) {
        currentView = viewName;
        cardLayout.show(mainContainer, viewName);

        // Highlight active sidebar button
        for (java.util.Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            if (entry.getKey().equals(viewName)) {
                entry.getValue().setBackground(new Color(105, 70, 52));
                entry.getValue().setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(AMBER, 1),
                        BorderFactory.createEmptyBorder(0, 16, 0, 10)
                ));
            } else {
                entry.getValue().setBackground(new Color(68, 46, 35));
                entry.getValue().setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(98, 70, 54), 1),
                        BorderFactory.createEmptyBorder(0, 16, 0, 10)
                ));
            }
        }

        if ("OVERVIEW".equals(viewName)) {
            loadOverviewData();
        } else if ("COMPLAINTS".equals(viewName)) {
            loadComplaintsData();
        } else if ("BILLING".equals(viewName)) {
            loadBillingData();
        } else if ("DIRECTORY".equals(viewName)) {
            loadDirectoryData();
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

        complaintsTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        complaintsTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        complaintsTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        complaintsTable.getColumnModel().getColumn(3).setPreferredWidth(185);
        complaintsTable.getColumnModel().getColumn(4).setPreferredWidth(95);
        complaintsTable.getColumnModel().getColumn(5).setPreferredWidth(125);
        complaintsTable.getColumnModel().getColumn(6).setPreferredWidth(115);
        complaintsTable.getColumnModel().getColumn(7).setPreferredWidth(110);

        complaintsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                Object statusVal = table.getValueAt(row, 6);

                if (!isSelected) {
                    if (statusVal != null && (statusVal.toString().equalsIgnoreCase("RESOLVED") || statusVal.toString().equalsIgnoreCase("COMPLETED"))) {
                        c.setForeground(new Color(24, 134, 75));
                        if (col == 6) c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if (statusVal != null && statusVal.toString().toUpperCase().contains("IN PROGRESS")) {
                        c.setForeground(new Color(205, 115, 25));
                        if (col == 6) c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if (statusVal != null && statusVal.toString().toUpperCase().contains("ASSIGNED")) {
                        c.setForeground(new Color(29, 111, 173));
                        if (col == 6) c.setFont(c.getFont().deriveFont(Font.BOLD));
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

        paymentsTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        paymentsTable.getColumnModel().getColumn(1).setPreferredWidth(130);
        paymentsTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        paymentsTable.getColumnModel().getColumn(3).setPreferredWidth(180);
        paymentsTable.getColumnModel().getColumn(4).setPreferredWidth(95);
        paymentsTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        paymentsTable.getColumnModel().getColumn(6).setPreferredWidth(90);
        paymentsTable.getColumnModel().getColumn(7).setPreferredWidth(85);
        paymentsTable.getColumnModel().getColumn(8).setPreferredWidth(90);

        paymentsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                Object statusVal = table.getValueAt(row, 8);

                if (!isSelected) {
                    if (statusVal != null && statusVal.toString().contains("OVERDUE")) {
                        c.setForeground(new Color(195, 35, 35));
                        if (col == 8) c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if (statusVal != null && statusVal.toString().contains("PAID")) {
                        c.setForeground(new Color(24, 134, 75));
                        if (col == 8) c.setFont(c.getFont().deriveFont(Font.BOLD));
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
    // 4. REGISTRATION VIEW (RESIDENT & WORKER)
    // ==========================================
    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(CREAM);
        panel.add(createTopBar("Registration Portal — Add Residents & Workers"));

        // Left Card: Resident Registration
        JPanel resCard = new JPanel(null);
        resCard.setBounds(30, 80, 435, 620);
        resCard.setBackground(Color.WHITE);
        resCard.setBorder(BorderFactory.createLineBorder(LINE, 1));
        panel.add(resCard);

        JLabel resTitle = new JLabel("Register New Resident");
        resTitle.setBounds(24, 16, 385, 24);
        resTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        resTitle.setForeground(COFFEE);
        resCard.add(resTitle);

        JLabel resSub = new JLabel("Create profile, credentials, and flat details");
        resSub.setBounds(24, 40, 385, 18);
        resSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resSub.setForeground(new Color(130, 105, 85));
        resCard.add(resSub);

        JSeparator sep1 = new JSeparator();
        sep1.setBounds(24, 64, 385, 1);
        sep1.setForeground(LINE);
        resCard.add(sep1);

        int ry = 70;
        int step = 56;

        JLabel nameLbl = createFormLabel("Full Name *");
        nameLbl.setBounds(24, ry, 385, 16);
        resCard.add(nameLbl);
        styleField(regResNameField);
        regResNameField.setBounds(24, ry + 18, 385, 32);
        resCard.add(regResNameField);

        ry += step;
        JLabel emailLbl = createFormLabel("Email Address * (Login ID)");
        emailLbl.setBounds(24, ry, 385, 16);
        resCard.add(emailLbl);
        styleField(regResEmailField);
        regResEmailField.setBounds(24, ry + 18, 385, 32);
        resCard.add(regResEmailField);

        ry += step;
        JLabel phoneLbl = createFormLabel("Phone Number *");
        phoneLbl.setBounds(24, ry, 385, 16);
        resCard.add(phoneLbl);
        styleField(regResPhoneField);
        regResPhoneField.setBounds(24, ry + 18, 385, 32);
        resCard.add(regResPhoneField);

        ry += step;
        JLabel aptLbl = createFormLabel("Apartment Number * (e.g. A-101, B-402)");
        aptLbl.setBounds(24, ry, 385, 16);
        resCard.add(aptLbl);
        styleField(regResAptField);
        regResAptField.setBounds(24, ry + 18, 385, 32);
        resCard.add(regResAptField);

        ry += step;
        JLabel passLbl = createFormLabel("Initial Password *");
        passLbl.setBounds(24, ry, 385, 16);
        resCard.add(passLbl);
        styleField(regResPassField);
        regResPassField.setBounds(24, ry + 18, 385, 32);
        resCard.add(regResPassField);

        ry += step;
        JLabel vehLbl = createFormLabel("Vehicle Number (Optional, e.g. KA-01-AB-1234)");
        vehLbl.setBounds(24, ry, 385, 16);
        resCard.add(vehLbl);
        styleField(regResVehicleField);
        regResVehicleField.setBounds(24, ry + 18, 385, 32);
        resCard.add(regResVehicleField);

        ry += step;
        JLabel parkLbl = createFormLabel("Assigned Parking Slot (Optional, e.g. A-01, P-12)");
        parkLbl.setBounds(24, ry, 385, 16);
        resCard.add(parkLbl);
        styleField(regResParkingField);
        regResParkingField.setBounds(24, ry + 18, 385, 32);
        resCard.add(regResParkingField);

        JButton regResBtn = new JButton("✓ Register Resident");
        regResBtn.setBounds(24, 480, 245, 38);
        regResBtn.setBackground(new Color(40, 135, 75));
        regResBtn.setForeground(Color.WHITE);
        regResBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        regResBtn.setFocusPainted(false);
        regResBtn.setBorder(BorderFactory.createLineBorder(new Color(30, 110, 60), 1));
        regResBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        regResBtn.addActionListener(e -> registerResident());
        resCard.add(regResBtn);

        JButton clearResBtn = new JButton("Clear");
        clearResBtn.setBounds(280, 480, 129, 38);
        clearResBtn.setBackground(new Color(242, 231, 215));
        clearResBtn.setForeground(COFFEE);
        clearResBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        clearResBtn.setFocusPainted(false);
        clearResBtn.setBorder(BorderFactory.createLineBorder(LINE, 1));
        clearResBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearResBtn.addActionListener(e -> clearResidentForm());
        resCard.add(clearResBtn);

        JPanel resInfoBox = new JPanel(null);
        resInfoBox.setBounds(24, 532, 385, 70);
        resInfoBox.setBackground(new Color(248, 244, 238));
        resInfoBox.setBorder(BorderFactory.createLineBorder(new Color(230, 218, 203), 1));
        JLabel resInfoIcon = new JLabel("ℹ️");
        resInfoIcon.setBounds(12, 12, 24, 24);
        resInfoBox.add(resInfoIcon);
        JLabel resInfoTxt = new JLabel("<html><b>Resident Portal Access:</b> Registered residents can immediately log in to submit complaints, invite visitors, and view society billing records.</html>");
        resInfoTxt.setBounds(38, 8, 335, 52);
        resInfoTxt.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        resInfoTxt.setForeground(new Color(85, 60, 45));
        resInfoBox.add(resInfoTxt);
        resCard.add(resInfoBox);

        // Right Card: Worker Registration
        JPanel workerCard = new JPanel(null);
        workerCard.setBounds(495, 80, 435, 620);
        workerCard.setBackground(Color.WHITE);
        workerCard.setBorder(BorderFactory.createLineBorder(LINE, 1));
        panel.add(workerCard);

        JLabel workerTitle = new JLabel("Register New Worker / Staff");
        workerTitle.setBounds(24, 16, 385, 24);
        workerTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        workerTitle.setForeground(COFFEE);
        workerCard.add(workerTitle);

        JLabel workerSub = new JLabel("Assign trade specialty role and staff login");
        workerSub.setBounds(24, 40, 385, 18);
        workerSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        workerSub.setForeground(new Color(130, 105, 85));
        workerCard.add(workerSub);

        JSeparator sep2 = new JSeparator();
        sep2.setBounds(24, 64, 385, 1);
        sep2.setForeground(LINE);
        workerCard.add(sep2);

        int wy = 70;

        JLabel wNameLbl = createFormLabel("Full Name *");
        wNameLbl.setBounds(24, wy, 385, 16);
        workerCard.add(wNameLbl);
        styleField(regWorkerNameField);
        regWorkerNameField.setBounds(24, wy + 18, 385, 32);
        workerCard.add(regWorkerNameField);

        wy += step;
        JLabel wEmailLbl = createFormLabel("Email Address * (Staff Login ID)");
        wEmailLbl.setBounds(24, wy, 385, 16);
        workerCard.add(wEmailLbl);
        styleField(regWorkerEmailField);
        regWorkerEmailField.setBounds(24, wy + 18, 385, 32);
        workerCard.add(regWorkerEmailField);

        wy += step;
        JLabel wPhoneLbl = createFormLabel("Phone Number *");
        wPhoneLbl.setBounds(24, wy, 385, 16);
        workerCard.add(wPhoneLbl);
        styleField(regWorkerPhoneField);
        regWorkerPhoneField.setBounds(24, wy + 18, 385, 32);
        workerCard.add(regWorkerPhoneField);

        wy += step;
        JLabel wRoleLbl = createFormLabel("Specialty / Trade Role *");
        wRoleLbl.setBounds(24, wy, 385, 16);
        workerCard.add(wRoleLbl);
        regWorkerTypeCombo.setBounds(24, wy + 18, 385, 32);
        regWorkerTypeCombo.setBackground(Color.WHITE);
        regWorkerTypeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        workerCard.add(regWorkerTypeCombo);

        wy += step;
        JLabel wPassLbl = createFormLabel("Initial Password *");
        wPassLbl.setBounds(24, wy, 385, 16);
        workerCard.add(wPassLbl);
        styleField(regWorkerPassField);
        regWorkerPassField.setBounds(24, wy + 18, 385, 32);
        workerCard.add(regWorkerPassField);

        // Explanatory Banner
        JPanel wBanner = new JPanel(null);
        wBanner.setBounds(24, 370, 385, 90);
        wBanner.setBackground(new Color(248, 244, 238));
        wBanner.setBorder(BorderFactory.createLineBorder(new Color(230, 218, 203), 1));
        JLabel wBannerIcon = new JLabel("🛠️");
        wBannerIcon.setBounds(12, 14, 24, 24);
        wBanner.add(wBannerIcon);
        JLabel wBannerTxt = new JLabel("<html><b>Smart Dispatch Integration:</b> Once registered, this worker will automatically be matched and assigned incoming complaints categorized under their specialty (e.g. Electrical, Plumbing, Security).</html>");
        wBannerTxt.setBounds(40, 10, 332, 68);
        wBannerTxt.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        wBannerTxt.setForeground(new Color(85, 60, 45));
        wBanner.add(wBannerTxt);
        workerCard.add(wBanner);

        JButton regWorkerBtn = new JButton("✓ Register Worker");
        regWorkerBtn.setBounds(24, 480, 245, 38);
        regWorkerBtn.setBackground(AMBER);
        regWorkerBtn.setForeground(Color.WHITE);
        regWorkerBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        regWorkerBtn.setFocusPainted(false);
        regWorkerBtn.setBorder(BorderFactory.createLineBorder(new Color(160, 95, 38), 1));
        regWorkerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        regWorkerBtn.addActionListener(e -> registerWorker());
        workerCard.add(regWorkerBtn);

        JButton clearWorkerBtn = new JButton("Clear");
        clearWorkerBtn.setBounds(280, 480, 129, 38);
        clearWorkerBtn.setBackground(new Color(242, 231, 215));
        clearWorkerBtn.setForeground(COFFEE);
        clearWorkerBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        clearWorkerBtn.setFocusPainted(false);
        clearWorkerBtn.setBorder(BorderFactory.createLineBorder(LINE, 1));
        clearWorkerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        clearWorkerBtn.addActionListener(e -> clearWorkerForm());
        workerCard.add(clearWorkerBtn);

        JPanel wFooterBox = new JPanel(null);
        wFooterBox.setBounds(24, 532, 385, 70);
        wFooterBox.setBackground(new Color(254, 250, 244));
        wFooterBox.setBorder(BorderFactory.createLineBorder(new Color(236, 224, 209), 1));
        JLabel wFooterIcon = new JLabel("📱");
        wFooterIcon.setBounds(12, 12, 24, 24);
        wFooterBox.add(wFooterIcon);
        JLabel wFooterTxt = new JLabel("<html><b>Staff Dashboard:</b> Workers log into their dedicated dashboard to start/complete tasks, review assignments, and log completion notes.</html>");
        wFooterTxt.setBounds(38, 8, 335, 52);
        wFooterTxt.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        wFooterTxt.setForeground(new Color(85, 60, 45));
        wFooterBox.add(wFooterTxt);
        workerCard.add(wFooterBox);

        return panel;
    }

    // ==========================================
    // 5. DIRECTORY & USER MANAGEMENT VIEW
    // ==========================================
    private JPanel createDirectoryPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(CREAM);
        panel.add(createTopBar("Society Directory — Manage Residents & Staff"));

        // Tab Switchers
        dirResidentsTabBtn = new JButton("👥 Residents Directory");
        dirResidentsTabBtn.setBounds(30, 78, 440, 40);
        dirResidentsTabBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        dirResidentsTabBtn.setFocusPainted(false);
        dirResidentsTabBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        dirWorkersTabBtn = new JButton("🔧 Workers & Staff");
        dirWorkersTabBtn.setBounds(490, 78, 440, 40);
        dirWorkersTabBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        dirWorkersTabBtn.setFocusPainted(false);
        dirWorkersTabBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        panel.add(dirResidentsTabBtn);
        panel.add(dirWorkersTabBtn);

        dirCardLayout = new CardLayout();
        dirCardContainer = new JPanel(dirCardLayout);
        dirCardContainer.setBounds(30, 126, 900, 580);
        dirCardContainer.setOpaque(false);
        panel.add(dirCardContainer);

        // 1. Residents Sub-panel
        JPanel resDirPanel = new JPanel(null);
        resDirPanel.setBackground(Color.WHITE);
        resDirPanel.setBorder(BorderFactory.createLineBorder(LINE, 1));

        JLabel resSearchLbl = new JLabel("Search:");
        resSearchLbl.setBounds(16, 14, 50, 30);
        resSearchLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        resSearchLbl.setForeground(COFFEE);
        resDirPanel.add(resSearchLbl);

        JTextField resSearchField = new JTextField();
        styleField(resSearchField);
        resSearchField.setBounds(68, 14, 210, 32);
        resDirPanel.add(resSearchField);

        JButton resClearBtn = new JButton("Clear");
        resClearBtn.setBounds(286, 14, 65, 32);
        resClearBtn.setBackground(new Color(242, 231, 215));
        resClearBtn.setForeground(COFFEE);
        resClearBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        resClearBtn.setBorder(BorderFactory.createLineBorder(LINE, 1));
        resClearBtn.setFocusPainted(false);
        resClearBtn.addActionListener(e -> {
            resSearchField.setText("");
            if (residentsSorter != null) residentsSorter.setRowFilter(null);
        });
        resDirPanel.add(resClearBtn);

        JButton editResBtn = new JButton("✏️ Edit Resident");
        editResBtn.setBounds(465, 14, 130, 32);
        editResBtn.setBackground(new Color(242, 231, 215));
        editResBtn.setForeground(COFFEE);
        editResBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        editResBtn.setBorder(BorderFactory.createLineBorder(LINE, 1));
        editResBtn.setFocusPainted(false);
        editResBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        editResBtn.addActionListener(e -> editSelectedResident());
        resDirPanel.add(editResBtn);

        JButton delResBtn = new JButton("🗑️ Remove Resident");
        delResBtn.setBounds(603, 14, 145, 32);
        delResBtn.setBackground(new Color(245, 222, 220));
        delResBtn.setForeground(new Color(165, 30, 30));
        delResBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        delResBtn.setBorder(BorderFactory.createLineBorder(new Color(225, 180, 175), 1));
        delResBtn.setFocusPainted(false);
        delResBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        delResBtn.addActionListener(e -> deleteSelectedResident());
        resDirPanel.add(delResBtn);

        JButton refreshResBtn = new JButton("🔄 Refresh");
        refreshResBtn.setBounds(756, 14, 126, 32);
        refreshResBtn.setBackground(new Color(242, 231, 215));
        refreshResBtn.setForeground(COFFEE);
        refreshResBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshResBtn.setBorder(BorderFactory.createLineBorder(LINE, 1));
        refreshResBtn.setFocusPainted(false);
        refreshResBtn.addActionListener(e -> loadDirectoryData());
        resDirPanel.add(refreshResBtn);

        styleTable(residentsTable);
        residentsTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        residentsTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        residentsTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        residentsTable.getColumnModel().getColumn(3).setPreferredWidth(170);
        residentsTable.getColumnModel().getColumn(4).setPreferredWidth(105);
        residentsTable.getColumnModel().getColumn(5).setPreferredWidth(105);
        residentsTable.getColumnModel().getColumn(6).setPreferredWidth(90);
        residentsTable.getColumnModel().getColumn(7).setPreferredWidth(95);

        residentsSorter = new TableRowSorter<>(residentsModel);
        residentsTable.setRowSorter(residentsSorter);

        resSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String txt = resSearchField.getText().trim();
                if (txt.isEmpty()) {
                    residentsSorter.setRowFilter(null);
                } else {
                    residentsSorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(txt)));
                }
            }
        });

        residentsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedResident();
                }
            }
        });

        JScrollPane resScroll = new JScrollPane(residentsTable);
        resScroll.setBounds(16, 56, 866, 475);
        resScroll.setBorder(BorderFactory.createLineBorder(LINE, 1));
        resDirPanel.add(resScroll);

        residentsCountSummary.setBounds(18, 540, 860, 24);
        residentsCountSummary.setFont(new Font("Segoe UI", Font.BOLD, 12));
        residentsCountSummary.setForeground(COFFEE);
        resDirPanel.add(residentsCountSummary);

        // 2. Workers Sub-panel
        JPanel workerDirPanel = new JPanel(null);
        workerDirPanel.setBackground(Color.WHITE);
        workerDirPanel.setBorder(BorderFactory.createLineBorder(LINE, 1));

        JLabel wSearchLbl = new JLabel("Search:");
        wSearchLbl.setBounds(16, 14, 50, 30);
        wSearchLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        wSearchLbl.setForeground(COFFEE);
        workerDirPanel.add(wSearchLbl);

        JTextField workerSearchField = new JTextField();
        styleField(workerSearchField);
        workerSearchField.setBounds(68, 14, 160, 32);
        workerDirPanel.add(workerSearchField);

        workerRoleFilterCombo = new JComboBox<>();
        workerRoleFilterCombo.addItem("All Roles");
        workerRoleFilterCombo.setBounds(236, 14, 140, 32);
        workerRoleFilterCombo.setBackground(Color.WHITE);
        workerRoleFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        workerDirPanel.add(workerRoleFilterCombo);

        JButton workerClearBtn = new JButton("Clear");
        workerClearBtn.setBounds(384, 14, 65, 32);
        workerClearBtn.setBackground(new Color(242, 231, 215));
        workerClearBtn.setForeground(COFFEE);
        workerClearBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        workerClearBtn.setBorder(BorderFactory.createLineBorder(LINE, 1));
        workerClearBtn.setFocusPainted(false);
        workerClearBtn.addActionListener(e -> {
            workerSearchField.setText("");
            workerRoleFilterCombo.setSelectedIndex(0);
            if (workersSorter != null) workersSorter.setRowFilter(null);
        });
        workerDirPanel.add(workerClearBtn);

        JButton editWorkerBtn = new JButton("✏️ Edit Worker");
        editWorkerBtn.setBounds(465, 14, 130, 32);
        editWorkerBtn.setBackground(new Color(242, 231, 215));
        editWorkerBtn.setForeground(COFFEE);
        editWorkerBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        editWorkerBtn.setBorder(BorderFactory.createLineBorder(LINE, 1));
        editWorkerBtn.setFocusPainted(false);
        editWorkerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        editWorkerBtn.addActionListener(e -> editSelectedWorker());
        workerDirPanel.add(editWorkerBtn);

        JButton delWorkerBtn = new JButton("🗑️ Remove Worker");
        delWorkerBtn.setBounds(603, 14, 145, 32);
        delWorkerBtn.setBackground(new Color(245, 222, 220));
        delWorkerBtn.setForeground(new Color(165, 30, 30));
        delWorkerBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        delWorkerBtn.setBorder(BorderFactory.createLineBorder(new Color(225, 180, 175), 1));
        delWorkerBtn.setFocusPainted(false);
        delWorkerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        delWorkerBtn.addActionListener(e -> deleteSelectedWorker());
        workerDirPanel.add(delWorkerBtn);

        JButton refreshWorkerBtn = new JButton("🔄 Refresh");
        refreshWorkerBtn.setBounds(756, 14, 126, 32);
        refreshWorkerBtn.setBackground(new Color(242, 231, 215));
        refreshWorkerBtn.setForeground(COFFEE);
        refreshWorkerBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshWorkerBtn.setBorder(BorderFactory.createLineBorder(LINE, 1));
        refreshWorkerBtn.setFocusPainted(false);
        refreshWorkerBtn.addActionListener(e -> loadDirectoryData());
        workerDirPanel.add(refreshWorkerBtn);

        styleTable(workersTable);
        workersTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        workersTable.getColumnModel().getColumn(1).setPreferredWidth(145);
        workersTable.getColumnModel().getColumn(2).setPreferredWidth(125);
        workersTable.getColumnModel().getColumn(3).setPreferredWidth(170);
        workersTable.getColumnModel().getColumn(4).setPreferredWidth(105);
        workersTable.getColumnModel().getColumn(5).setPreferredWidth(85);
        workersTable.getColumnModel().getColumn(6).setPreferredWidth(85);
        workersTable.getColumnModel().getColumn(7).setPreferredWidth(95);

        workersSorter = new TableRowSorter<>(workersModel);
        workersTable.setRowSorter(workersSorter);

        Runnable applyWorkerFilter = () -> {
            String query = workerSearchField.getText().trim();
            String role = (String) workerRoleFilterCombo.getSelectedItem();
            List<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();
            if (!query.isEmpty()) {
                filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(query)));
            }
            if (role != null && !role.equals("All Roles")) {
                filters.add(RowFilter.regexFilter("(?i)^" + Pattern.quote(role) + "$", 2));
            }
            if (filters.isEmpty()) {
                workersSorter.setRowFilter(null);
            } else {
                workersSorter.setRowFilter(RowFilter.andFilter(filters));
            }
        };

        workerSearchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyWorkerFilter.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyWorkerFilter.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyWorkerFilter.run(); }
        });
        workerRoleFilterCombo.addActionListener(e -> applyWorkerFilter.run());

        workersTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    if (col == 5 && value != null) {
                        try {
                            int cnt = Integer.parseInt(value.toString());
                            if (cnt > 0) {
                                c.setForeground(new Color(205, 115, 25));
                                c.setFont(c.getFont().deriveFont(Font.BOLD));
                            } else {
                                c.setForeground(new Color(24, 134, 75));
                                c.setFont(c.getFont().deriveFont(Font.PLAIN));
                            }
                        } catch (Exception ex) {
                            c.setForeground(COFFEE);
                        }
                    } else {
                        c.setForeground(COFFEE);
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    }
                }
                return c;
            }
        });

        workersTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedWorker();
                }
            }
        });

        JScrollPane wScroll = new JScrollPane(workersTable);
        wScroll.setBounds(16, 56, 866, 475);
        wScroll.setBorder(BorderFactory.createLineBorder(LINE, 1));
        workerDirPanel.add(wScroll);

        workersCountSummary.setBounds(18, 540, 860, 24);
        workersCountSummary.setFont(new Font("Segoe UI", Font.BOLD, 12));
        workersCountSummary.setForeground(COFFEE);
        workerDirPanel.add(workersCountSummary);

        dirCardContainer.add(resDirPanel, "DIR_RESIDENTS");
        dirCardContainer.add(workerDirPanel, "DIR_WORKERS");

        dirResidentsTabBtn.addActionListener(e -> setDirectorySubView("DIR_RESIDENTS"));
        dirWorkersTabBtn.addActionListener(e -> setDirectorySubView("DIR_WORKERS"));

        setDirectorySubView("DIR_RESIDENTS");
        return panel;
    }

    private void setDirectorySubView(String subView) {
        dirCardLayout.show(dirCardContainer, subView);
        if ("DIR_RESIDENTS".equals(subView)) {
            dirResidentsTabBtn.setBackground(COFFEE);
            dirResidentsTabBtn.setForeground(CREAM);
            dirResidentsTabBtn.setBorder(BorderFactory.createLineBorder(new Color(35, 20, 15), 1));
            dirWorkersTabBtn.setBackground(new Color(242, 231, 215));
            dirWorkersTabBtn.setForeground(COFFEE);
            dirWorkersTabBtn.setBorder(BorderFactory.createLineBorder(LINE, 1));
        } else {
            dirWorkersTabBtn.setBackground(COFFEE);
            dirWorkersTabBtn.setForeground(CREAM);
            dirWorkersTabBtn.setBorder(BorderFactory.createLineBorder(new Color(35, 20, 15), 1));
            dirResidentsTabBtn.setBackground(new Color(242, 231, 215));
            dirResidentsTabBtn.setForeground(COFFEE);
            dirResidentsTabBtn.setBorder(BorderFactory.createLineBorder(LINE, 1));
        }
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
        if (title.length() > 255) {
            title = title.substring(0, 255);
        }
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
                     "COALESCE(w.full_name, 'Unassigned') AS worker_name, c.status, " +
                     "c.created_at::DATE AS log_date " +
                     "FROM complaints c " +
                     "JOIN residents r ON c.resident_id = r.resident_id " +
                     "LEFT JOIN workers w ON c.worker_id = w.worker_id " +
                     "ORDER BY c.complaint_id DESC";

        int total = 0, pending = 0, inProg = 0, completed = 0;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                total++;
                int id = rs.getInt("complaint_id");
                String resName = rs.getString("full_name");
                String apt = rs.getString("apartment_no");
                String title = rs.getString("title");
                String cat = rs.getString("category");
                String worker = rs.getString("worker_name");
                java.sql.Date logDate = rs.getDate("log_date");
                String rawStatus = rs.getString("status");
                String displayStatus = rawStatus != null ? rawStatus.replace("_", " ") : "PENDING";

                if ("COMPLETED".equalsIgnoreCase(rawStatus) || "RESOLVED".equalsIgnoreCase(rawStatus)) {
                    completed++;
                } else if ("IN_PROGRESS".equalsIgnoreCase(rawStatus) || "IN PROGRESS".equalsIgnoreCase(rawStatus)) {
                    inProg++;
                } else {
                    pending++;
                }

                complaintsModel.addRow(new Object[]{
                        id, resName, apt, title, cat, worker,
                        displayStatus,
                        logDate != null ? logDate.toString() : "-"
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
                     "p.payment_date, p.payment_method, p.status " +
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
    // REGISTRATION HANDLERS
    // ==========================================
    private void registerResident() {
        String name = regResNameField.getText().trim();
        String email = regResEmailField.getText().trim().toLowerCase();
        String phone = regResPhoneField.getText().trim();
        String apt = regResAptField.getText().trim();
        String pass = new String(regResPassField.getPassword()).trim();
        String vehicle = regResVehicleField.getText().trim();
        String parking = regResParkingField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || apt.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields marked with *.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Invalid Email", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;

            // Check email uniqueness across all tables
            String checkSql = "SELECT 'resident' FROM residents WHERE lower(email) = ? " +
                              "UNION SELECT 'worker' FROM workers WHERE lower(email) = ? " +
                              "UNION SELECT 'admin' FROM admins WHERE lower(email) = ?";
            try (PreparedStatement ps = con.prepareStatement(checkSql)) {
                ps.setString(1, email);
                ps.setString(2, email);
                ps.setString(3, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        JOptionPane.showMessageDialog(this, "The email '" + email + "' is already registered in the system.\nPlease use a unique email.", "Email Already Exists", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            String insSql = "INSERT INTO residents (full_name, email, phone, apartment_no, password, vehicle_no, parking_slot) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(insSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, phone);
                ps.setString(4, apt);
                ps.setString(5, pass);
                ps.setString(6, vehicle.isEmpty() ? null : vehicle);
                ps.setString(7, parking.isEmpty() ? null : parking);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    int newResId = 0;
                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) newResId = gk.getInt(1);
                    }

                    if (!parking.isEmpty() && newResId > 0) {
                        try (PreparedStatement pps = con.prepareStatement(
                                "INSERT INTO parking (slot_number, parking_type, status, resident_id) " +
                                "VALUES (?, 'RESIDENT', 'OCCUPIED', ?) " +
                                "ON CONFLICT (slot_number) DO UPDATE SET resident_id = EXCLUDED.resident_id, status = 'OCCUPIED'")) {
                            pps.setString(1, parking);
                            pps.setInt(2, newResId);
                            pps.executeUpdate();
                        } catch (Exception ignored) {}
                    }

                    JOptionPane.showMessageDialog(this,
                            "Resident registered successfully!\n\n" +
                            "Name: " + name + "\n" +
                            "Apartment: " + apt + "\n" +
                            "Login Email: " + email + "\n" +
                            (newResId > 0 ? "Resident ID: #" + newResId : ""),
                            "Registration Successful", JOptionPane.INFORMATION_MESSAGE);

                    clearResidentForm();
                    loadOverviewData();
                    loadDirectoryData();
                }
            }
        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
        }
    }

    private void clearResidentForm() {
        regResNameField.setText("");
        regResEmailField.setText("");
        regResPhoneField.setText("");
        regResAptField.setText("");
        regResPassField.setText("");
        regResVehicleField.setText("");
        regResParkingField.setText("");
    }

    private void registerWorker() {
        String name = regWorkerNameField.getText().trim();
        String email = regWorkerEmailField.getText().trim().toLowerCase();
        String phone = regWorkerPhoneField.getText().trim();
        String type = (String) regWorkerTypeCombo.getSelectedItem();
        String pass = new String(regWorkerPassField.getPassword()).trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields marked with *.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Invalid Email", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;

            // Check email uniqueness across all tables
            String checkSql = "SELECT 'resident' FROM residents WHERE lower(email) = ? " +
                              "UNION SELECT 'worker' FROM workers WHERE lower(email) = ? " +
                              "UNION SELECT 'admin' FROM admins WHERE lower(email) = ?";
            try (PreparedStatement ps = con.prepareStatement(checkSql)) {
                ps.setString(1, email);
                ps.setString(2, email);
                ps.setString(3, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        JOptionPane.showMessageDialog(this, "The email '" + email + "' is already registered in the system.\nPlease use a unique email.", "Email Already Exists", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            String insSql = "INSERT INTO workers (full_name, email, phone, worker_type, password) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(insSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.setString(2, email);
                ps.setString(3, phone);
                ps.setString(4, type);
                ps.setString(5, pass);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    int newWorkerId = 0;
                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) newWorkerId = gk.getInt(1);
                    }

                    JOptionPane.showMessageDialog(this,
                            "Worker registered successfully!\n\n" +
                            "Name: " + name + "\n" +
                            "Specialty: " + type + "\n" +
                            "Login Email: " + email + "\n" +
                            (newWorkerId > 0 ? "Worker ID: #" + newWorkerId : "") + "\n\n" +
                            "Worker is now active and ready for maintenance ticket assignment.",
                            "Registration Successful", JOptionPane.INFORMATION_MESSAGE);

                    clearWorkerForm();
                    loadOverviewData();
                    loadComplaintsData();
                    loadDirectoryData();
                }
            }
        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
        }
    }

    private void clearWorkerForm() {
        regWorkerNameField.setText("");
        regWorkerEmailField.setText("");
        regWorkerPhoneField.setText("");
        regWorkerTypeCombo.setSelectedIndex(0);
        regWorkerPassField.setText("");
    }

    // ==========================================
    // DIRECTORY HANDLERS & CRUD
    // ==========================================
    private void loadDirectoryData() {
        residentsModel.setRowCount(0);
        workersModel.setRowCount(0);

        int resCount = 0;
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;

            String resSql = "SELECT resident_id, full_name, apartment_no, email, phone, vehicle_no, parking_slot, created_at::DATE AS reg_date " +
                            "FROM residents ORDER BY apartment_no ASC, resident_id ASC";
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(resSql)) {
                while (rs.next()) {
                    resCount++;
                    java.sql.Date d = rs.getDate("reg_date");
                    residentsModel.addRow(new Object[]{
                            rs.getInt("resident_id"),
                            rs.getString("full_name"),
                            rs.getString("apartment_no"),
                            rs.getString("email"),
                            rs.getString("phone") != null ? rs.getString("phone") : "-",
                            rs.getString("vehicle_no") != null ? rs.getString("vehicle_no") : "-",
                            rs.getString("parking_slot") != null ? rs.getString("parking_slot") : "-",
                            d != null ? d.toString() : "-"
                    });
                }
            }
            residentsCountSummary.setText("Total Residents: " + resCount + " | Double-click any resident row to view / edit profile");
            if (dirResidentsTabBtn != null) {
                dirResidentsTabBtn.setText("👥 Residents Directory (" + resCount + ")");
            }

            // Load distinct roles for filter combo
            if (workerRoleFilterCombo != null) {
                String curSelected = (String) workerRoleFilterCombo.getSelectedItem();
                workerRoleFilterCombo.removeAllItems();
                workerRoleFilterCombo.addItem("All Roles");
                try (Statement st = con.createStatement();
                     ResultSet rs = st.executeQuery("SELECT DISTINCT worker_type FROM workers WHERE worker_type IS NOT NULL AND worker_type != '' ORDER BY worker_type")) {
                    while (rs.next()) {
                        workerRoleFilterCombo.addItem(rs.getString(1));
                    }
                }
                if (curSelected != null) {
                    workerRoleFilterCombo.setSelectedItem(curSelected);
                }
            }

            // Load workers
            int workerCount = 0;
            String wSql = "SELECT w.worker_id, w.full_name, w.worker_type, w.email, w.phone, w.created_at::DATE AS join_date, " +
                          "COUNT(CASE WHEN c.status IN ('ASSIGNED', 'IN_PROGRESS', 'IN PROGRESS') THEN 1 END) AS active_cnt, " +
                          "COUNT(CASE WHEN c.status IN ('COMPLETED', 'RESOLVED') THEN 1 END) AS resolved_cnt " +
                          "FROM workers w " +
                          "LEFT JOIN complaints c ON w.worker_id = c.worker_id " +
                          "GROUP BY w.worker_id, w.full_name, w.worker_type, w.email, w.phone, w.created_at " +
                          "ORDER BY w.worker_id ASC";
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(wSql)) {
                while (rs.next()) {
                    workerCount++;
                    java.sql.Date d = rs.getDate("join_date");
                    workersModel.addRow(new Object[]{
                            rs.getInt("worker_id"),
                            rs.getString("full_name"),
                            rs.getString("worker_type"),
                            rs.getString("email"),
                            rs.getString("phone") != null ? rs.getString("phone") : "-",
                            rs.getInt("active_cnt"),
                            rs.getInt("resolved_cnt"),
                            d != null ? d.toString() : "-"
                    });
                }
            }
            workersCountSummary.setText("Total Workers: " + workerCount + " | Double-click any worker row to view / edit profile");
            if (dirWorkersTabBtn != null) {
                dirWorkersTabBtn.setText("🔧 Workers & Staff (" + workerCount + ")");
            }

        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
        }
    }

    private void editSelectedResident() {
        int viewRow = residentsTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a resident from the table first.", "Select Resident", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = residentsTable.convertRowIndexToModel(viewRow);
        int resId = (int) residentsModel.getValueAt(modelRow, 0);

        String curName = "", curEmail = "", curPhone = "", curApt = "", curVehicle = "", curParking = "";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT full_name, email, phone, apartment_no, vehicle_no, parking_slot FROM residents WHERE resident_id = ?")) {
            ps.setInt(1, resId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    curName = rs.getString("full_name");
                    curEmail = rs.getString("email");
                    curPhone = rs.getString("phone") != null ? rs.getString("phone") : "";
                    curApt = rs.getString("apartment_no");
                    curVehicle = rs.getString("vehicle_no") != null ? rs.getString("vehicle_no") : "";
                    curParking = rs.getString("parking_slot") != null ? rs.getString("parking_slot") : "";
                }
            }
        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
            return;
        }

        JTextField nameField = new JTextField(curName);
        JTextField emailField = new JTextField(curEmail);
        emailField.setEditable(false);
        emailField.setBackground(new Color(245, 242, 237));
        JTextField phoneField = new JTextField(curPhone);
        JTextField aptField = new JTextField(curApt);
        JTextField vehField = new JTextField(curVehicle);
        JTextField parkField = new JTextField(curParking);
        JPasswordField passField = new JPasswordField();

        JPanel form = new JPanel(new GridLayout(0, 1, 4, 4));
        form.setPreferredSize(new Dimension(380, 360));
        form.add(new JLabel("Full Name *:"));
        form.add(nameField);
        form.add(new JLabel("Email Address (Primary Login ID - Read Only):"));
        form.add(emailField);
        form.add(new JLabel("Apartment Number *:"));
        form.add(aptField);
        form.add(new JLabel("Phone Number *:"));
        form.add(phoneField);
        form.add(new JLabel("Vehicle Number:"));
        form.add(vehField);
        form.add(new JLabel("Assigned Parking Slot:"));
        form.add(parkField);
        form.add(new JLabel("Reset Password (Leave blank to keep current):"));
        form.add(passField);

        int result = JOptionPane.showConfirmDialog(this, form, "Edit Resident Details — ID #" + resId, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String newName = nameField.getText().trim();
        String newApt = aptField.getText().trim();
        String newPhone = phoneField.getText().trim();
        String newVeh = vehField.getText().trim();
        String newPark = parkField.getText().trim();
        String newPass = new String(passField.getPassword()).trim();

        if (newName.isEmpty() || newApt.isEmpty() || newPhone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full name, apartment number, and phone number cannot be blank.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String sql;
            if (newPass.isEmpty()) {
                sql = "UPDATE residents SET full_name = ?, phone = ?, apartment_no = ?, vehicle_no = ?, parking_slot = ? WHERE resident_id = ?";
            } else {
                sql = "UPDATE residents SET full_name = ?, phone = ?, apartment_no = ?, vehicle_no = ?, parking_slot = ?, password = ? WHERE resident_id = ?";
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, newName);
                ps.setString(2, newPhone);
                ps.setString(3, newApt);
                ps.setString(4, newVeh.isEmpty() ? null : newVeh);
                ps.setString(5, newPark.isEmpty() ? null : newPark);
                if (newPass.isEmpty()) {
                    ps.setInt(6, resId);
                } else {
                    ps.setString(6, newPass);
                    ps.setInt(7, resId);
                }
                ps.executeUpdate();
            }

            if (!newPark.isEmpty()) {
                try (PreparedStatement pps = con.prepareStatement(
                        "INSERT INTO parking (slot_number, parking_type, status, resident_id) " +
                        "VALUES (?, 'RESIDENT', 'OCCUPIED', ?) " +
                        "ON CONFLICT (slot_number) DO UPDATE SET resident_id = EXCLUDED.resident_id, status = 'OCCUPIED'")) {
                    pps.setString(1, newPark);
                    pps.setInt(2, resId);
                    pps.executeUpdate();
                } catch (Exception ignored) {}
            }

            JOptionPane.showMessageDialog(this, "Resident details updated successfully!");
            loadDirectoryData();
            loadOverviewData();
        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
        }
    }

    private void deleteSelectedResident() {
        int viewRow = residentsTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a resident to remove.", "Select Resident", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = residentsTable.convertRowIndexToModel(viewRow);
        int resId = (int) residentsModel.getValueAt(modelRow, 0);
        String resName = (String) residentsModel.getValueAt(modelRow, 1);
        String apt = (String) residentsModel.getValueAt(modelRow, 2);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to remove resident '" + resName + "' (" + apt + ")?\n\n" +
                "⚠️ WARNING: This will permanently delete this resident account\n" +
                "along with all associated complaints, billing dues, and visitor passes.",
                "Confirm Resident Removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM residents WHERE resident_id = ?")) {
            ps.setInt(1, resId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Resident '" + resName + "' removed successfully.");
                loadDirectoryData();
                loadOverviewData();
                loadComplaintsData();
                loadBillingData();
            }
        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
        }
    }

    private void editSelectedWorker() {
        int viewRow = workersTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a worker from the table first.", "Select Worker", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = workersTable.convertRowIndexToModel(viewRow);
        int workerId = (int) workersModel.getValueAt(modelRow, 0);

        String curName = "", curEmail = "", curPhone = "", curType = "";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT full_name, email, phone, worker_type FROM workers WHERE worker_id = ?")) {
            ps.setInt(1, workerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    curName = rs.getString("full_name");
                    curEmail = rs.getString("email");
                    curPhone = rs.getString("phone") != null ? rs.getString("phone") : "";
                    curType = rs.getString("worker_type") != null ? rs.getString("worker_type") : "";
                }
            }
        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
            return;
        }

        JTextField nameField = new JTextField(curName);
        JTextField emailField = new JTextField(curEmail);
        emailField.setEditable(false);
        emailField.setBackground(new Color(245, 242, 237));
        JTextField phoneField = new JTextField(curPhone);
        JComboBox<String> typeCombo = new JComboBox<>(new String[] {
                "Electrician", "Plumber", "Security", "Cleaning / Housekeeping",
                "Carpenter", "Gardener", "HVAC / AC Repair", "General Maintenance", "Other"
        });
        typeCombo.setEditable(true);
        typeCombo.setSelectedItem(curType);
        JPasswordField passField = new JPasswordField();

        JPanel form = new JPanel(new GridLayout(0, 1, 4, 4));
        form.setPreferredSize(new Dimension(380, 290));
        form.add(new JLabel("Full Name *:"));
        form.add(nameField);
        form.add(new JLabel("Email Address (Staff Login ID - Read Only):"));
        form.add(emailField);
        form.add(new JLabel("Specialty / Trade Role *:"));
        form.add(typeCombo);
        form.add(new JLabel("Phone Number *:"));
        form.add(phoneField);
        form.add(new JLabel("Reset Password (Leave blank to keep current):"));
        form.add(passField);

        int result = JOptionPane.showConfirmDialog(this, form, "Edit Worker Details — ID #" + workerId, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String newName = nameField.getText().trim();
        String newType = typeCombo.getSelectedItem() != null ? typeCombo.getSelectedItem().toString().trim() : "";
        String newPhone = phoneField.getText().trim();
        String newPass = new String(passField.getPassword()).trim();

        if (newName.isEmpty() || newType.isEmpty() || newPhone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full name, specialty, and phone number cannot be blank.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            String sql;
            if (newPass.isEmpty()) {
                sql = "UPDATE workers SET full_name = ?, phone = ?, worker_type = ? WHERE worker_id = ?";
            } else {
                sql = "UPDATE workers SET full_name = ?, phone = ?, worker_type = ?, password = ? WHERE worker_id = ?";
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, newName);
                ps.setString(2, newPhone);
                ps.setString(3, newType);
                if (newPass.isEmpty()) {
                    ps.setInt(4, workerId);
                } else {
                    ps.setString(4, newPass);
                    ps.setInt(5, workerId);
                }
                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Worker details updated successfully!");
            loadDirectoryData();
            loadOverviewData();
            loadComplaintsData();
        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
        }
    }

    private void deleteSelectedWorker() {
        int viewRow = workersTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a worker to remove.", "Select Worker", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = workersTable.convertRowIndexToModel(viewRow);
        int workerId = (int) workersModel.getValueAt(modelRow, 0);
        String workerName = (String) workersModel.getValueAt(modelRow, 1);
        String workerType = (String) workersModel.getValueAt(modelRow, 2);
        int activeTasks = (int) workersModel.getValueAt(modelRow, 5);

        String warnMsg = "Are you sure you want to remove worker '" + workerName + "' (" + workerType + ")?";
        if (activeTasks > 0) {
            warnMsg += "\n\n⚠️ NOTE: This worker currently has " + activeTasks + " active tasks.\n" +
                        "Removing this worker will reset those complaints to 'PENDING'\n" +
                        "so they can be reassigned.";
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                warnMsg,
                "Confirm Worker Removal",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Revert any active complaints assigned to this worker to PENDING
                try (PreparedStatement ps1 = con.prepareStatement(
                        "UPDATE complaints SET worker_id = NULL, status = 'PENDING' " +
                        "WHERE worker_id = ? AND status IN ('ASSIGNED', 'IN_PROGRESS', 'IN PROGRESS')")) {
                    ps1.setInt(1, workerId);
                    ps1.executeUpdate();
                }

                // Delete worker
                try (PreparedStatement ps2 = con.prepareStatement("DELETE FROM workers WHERE worker_id = ?")) {
                    ps2.setInt(1, workerId);
                    ps2.executeUpdate();
                }

                con.commit();
                JOptionPane.showMessageDialog(this, "Worker '" + workerName + "' removed successfully.");
                loadDirectoryData();
                loadOverviewData();
                loadComplaintsData();
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
        }
    }

    // ==========================================
    // HELPER UI METHODS
    // ==========================================
    private void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBackground(new Color(254, 252, 249));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE, 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private JLabel createFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(90, 60, 45));
        return lbl;
    }

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
        b.setBackground(new Color(68, 46, 35));
        b.setForeground(CREAM);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(98, 70, 54), 1),
                BorderFactory.createEmptyBorder(0, 16, 0, 10)
        ));
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
                } else if ("REGISTER".equals(type)) {
                    g2.setStroke(new BasicStroke(1.8f));
                    g2.drawOval(x + 1, y + 2, 6, 6);
                    g2.drawArc(x, y + 9, 8, 7, 0, 180);
                    g2.drawLine(x + 13, y + 4, x + 13, y + 12);
                    g2.drawLine(x + 9, y + 8, x + 17, y + 8);
                } else if ("DIRECTORY".equals(type)) {
                    g2.setStroke(new BasicStroke(1.8f));
                    g2.drawRoundRect(x + 1, y + 2, 16, 14, 2, 2);
                    g2.drawOval(x + 4, y + 5, 3, 3);
                    g2.drawLine(x + 9, y + 6, x + 14, y + 6);
                    g2.drawLine(x + 4, y + 11, x + 14, y + 11);
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
