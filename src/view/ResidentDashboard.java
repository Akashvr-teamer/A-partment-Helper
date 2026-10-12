package view;

import database.DBConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

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
            new String[] { "ID", "Visitor", "Phone", "Parking", "Status", "Valid Until" }, 0) {
        public boolean isCellEditable(int row, int col) { return false; }
    };

    // Complaints UI Components
    private JTextField complaintTitleField;
    private JComboBox<String> complaintCategoryCombo;
    private JTextArea complaintDescArea;
    private JLabel complaintsSummaryLabel;
    private final DefaultTableModel complaintsModel = new DefaultTableModel(
            new String[] { "ID", "Title", "Category", "Worker", "Status", "Date Logged", "Description" }, 0) {
        public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JTable complaintsTable = new JTable(complaintsModel);

    // Payments UI Components
    private JLabel totalDuesLbl;
    private JLabel overdueDuesLbl;
    private JLabel paidCountLbl;
    private final DefaultTableModel paymentsModel = new DefaultTableModel(
            new String[] { "ID", "Month / Year", "Amount (₹)", "Due Date", "Paid Date", "Payment Method", "Status", "Overdue Tracking" }, 0) {
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

        // App Name Header
        JLabel logo = new JLabel("A-partment");
        logo.setBounds(20, 25, 180, 30);
        logo.setForeground(CREAM);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        sidebar.add(logo);

        JLabel logoSub = new JLabel("Resident Portal");
        logoSub.setBounds(20, 55, 180, 20);
        logoSub.setForeground(new Color(185, 155, 130));
        logoSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sidebar.add(logoSub);

        // Sidebar buttons
        JButton overviewBtn = createSidebarButton("Overview", getNavIcon("OVERVIEW"));
        overviewBtn.setBounds(20, 100, 180, 42);
        overviewBtn.addActionListener(e -> showView("OVERVIEW"));
        sidebar.add(overviewBtn);

        JButton complaintsBtn = createSidebarButton("Complaints", getNavIcon("COMPLAINTS"));
        complaintsBtn.setBounds(20, 152, 180, 42);
        complaintsBtn.addActionListener(e -> showView("COMPLAINTS"));
        sidebar.add(complaintsBtn);

        JButton paymentsBtn = createSidebarButton("Payments", getNavIcon("PAYMENTS"));
        paymentsBtn.setBounds(20, 204, 180, 42);
        paymentsBtn.addActionListener(e -> showView("PAYMENTS"));
        sidebar.add(paymentsBtn);

        // Logout Button placed at the bottom
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

        // Add Views into the CardLayout Container
        mainContainer.setOpaque(false);
        mainContainer.add(createOverviewPanel(), "OVERVIEW");
        mainContainer.add(createComplaintsPanel(), "COMPLAINTS");
        mainContainer.add(createPaymentsPanel(), "PAYMENTS");
        root.add(mainContainer, BorderLayout.CENTER);

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

    // Common Top Header Bar for Portal Views matching App Colors (COFFEE theme)
    private JPanel createTopBar(String title) {
        JPanel header = new JPanel(null);
        header.setBounds(0, 0, 960, 65);
        header.setBackground(COFFEE);

        JLabel headerTitle = new JLabel(title);
        headerTitle.setBounds(30, 16, 450, 32);
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerTitle.setForeground(CREAM);
        header.add(headerTitle);

        JLabel subHeader = new JLabel("Resident: " + residentName + " (ID: #" + residentId + ")");
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
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(COFFEE);

        JLabel subLabel = new JLabel("Your apartment at a glance");
        subLabel.setForeground(new Color(126, 105, 86));
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

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
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        tableTitle.setForeground(COFFEE);
        tableHeader.add(tableTitle, BorderLayout.WEST);

        overviewStatus.setForeground(new Color(126, 105, 86));
        tableHeader.add(overviewStatus, BorderLayout.EAST);
        tablePanel.add(tableHeader, BorderLayout.NORTH);

        JTable table = new JTable(visitorModel);
        styleOverviewTable(table);
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
        JPanel panel = new JPanel(null);
        panel.setBackground(CREAM);

        // Top Header matching COFFEE theme (no close button)
        panel.add(createTopBar("Maintenance Complaints Portal"));

        // Left Panel: Raise New Complaint Form (Compact to give history more room)
        JPanel formPanel = new JPanel(null);
        formPanel.setBounds(25, 80, 275, 630);
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createLineBorder(LINE, 1));
        panel.add(formPanel);

        JLabel formTitle = new JLabel("Raise New Complaint");
        formTitle.setBounds(18, 15, 240, 25);
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        formTitle.setForeground(COFFEE);
        formPanel.add(formTitle);

        JLabel titleLbl = new JLabel("Complaint Title *");
        titleLbl.setBounds(18, 50, 240, 20);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(new Color(115, 82, 56));
        formPanel.add(titleLbl);

        complaintTitleField = new JTextField();
        complaintTitleField.setBounds(18, 72, 238, 32);
        complaintTitleField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        complaintTitleField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE, 1),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));
        formPanel.add(complaintTitleField);

        JLabel catLbl = new JLabel("Category *");
        catLbl.setBounds(18, 114, 240, 20);
        catLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        catLbl.setForeground(new Color(115, 82, 56));
        formPanel.add(catLbl);

        String[] categories = {"Plumbing", "Electrical", "Carpentry", "Lift", "Pest Control", "Cleaning", "Painting", "General Maintenance"};
        complaintCategoryCombo = new JComboBox<>(categories);
        complaintCategoryCombo.setBounds(18, 136, 238, 32);
        complaintCategoryCombo.setBackground(Color.WHITE);
        complaintCategoryCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        complaintCategoryCombo.setBorder(BorderFactory.createLineBorder(LINE, 1));
        formPanel.add(complaintCategoryCombo);

        JLabel descLbl = new JLabel("Description / Issue Details");
        descLbl.setBounds(18, 180, 240, 20);
        descLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        descLbl.setForeground(new Color(115, 82, 56));
        formPanel.add(descLbl);

        complaintDescArea = new JTextArea();
        complaintDescArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        complaintDescArea.setLineWrap(true);
        complaintDescArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(complaintDescArea);
        descScroll.setBounds(18, 205, 238, 325);
        descScroll.setBorder(BorderFactory.createLineBorder(LINE, 1));
        formPanel.add(descScroll);

        JButton submitBtn = LoginFrame.button("SUBMIT COMPLAINT");
        submitBtn.setBounds(18, 550, 238, 44);
        submitBtn.addActionListener(e -> submitComplaint());
        formPanel.add(submitBtn);

        // Right Panel: Complaint History (Expanded to 620px for extra breathing room)
        JPanel historyPanel = new JPanel(null);
        historyPanel.setBounds(315, 80, 620, 630);
        historyPanel.setBackground(Color.WHITE);
        historyPanel.setBorder(BorderFactory.createLineBorder(LINE, 1));
        panel.add(historyPanel);

        JLabel histTitle = new JLabel("My Complaint History & Overdue Status");
        histTitle.setBounds(20, 15, 430, 25);
        histTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        histTitle.setForeground(COFFEE);
        historyPanel.add(histTitle);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBounds(510, 14, 90, 28);
        refreshBtn.setBackground(new Color(242, 231, 215));
        refreshBtn.setForeground(COFFEE);
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setBorder(BorderFactory.createLineBorder(LINE, 1));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> loadComplaints());
        historyPanel.add(refreshBtn);

        complaintsSummaryLabel = new JLabel("Loading complaints...");
        complaintsSummaryLabel.setBounds(20, 43, 580, 20);
        complaintsSummaryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        complaintsSummaryLabel.setForeground(new Color(126, 105, 86));
        historyPanel.add(complaintsSummaryLabel);

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

        complaintsTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        complaintsTable.getColumnModel().getColumn(1).setPreferredWidth(175);
        complaintsTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        complaintsTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        complaintsTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        complaintsTable.getColumnModel().getColumn(5).setPreferredWidth(80);

        // Hide column 6 (Description) from direct table display; read via selection
        complaintsTable.removeColumn(complaintsTable.getColumnModel().getColumn(6));

        complaintsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                Object statusVal = table.getValueAt(row, 4);

                if (!isSelected) {
                    if (statusVal != null && (statusVal.toString().equalsIgnoreCase("RESOLVED") || statusVal.toString().equalsIgnoreCase("COMPLETED"))) {
                        c.setForeground(new Color(24, 134, 75));
                        if (col == 4) c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if (statusVal != null && statusVal.toString().toUpperCase().contains("IN PROGRESS")) {
                        c.setForeground(new Color(205, 115, 25));
                        if (col == 4) c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if (statusVal != null && statusVal.toString().toUpperCase().contains("ASSIGNED")) {
                        c.setForeground(new Color(29, 111, 173));
                        if (col == 4) c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setForeground(COFFEE);
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(complaintsTable);
        scrollPane.setBounds(20, 70, 580, 330);
        scrollPane.setBorder(BorderFactory.createLineBorder(LINE, 1));
        historyPanel.add(scrollPane);

        // Selected Complaint Detail & Description Pane
        JPanel detailBox = new JPanel(null);
        detailBox.setBounds(20, 412, 580, 198);
        detailBox.setBackground(new Color(253, 250, 246));
        detailBox.setBorder(BorderFactory.createLineBorder(LINE, 1));
        historyPanel.add(detailBox);

        JLabel detailHeader = new JLabel("Complaint Description & Issue Details");
        detailHeader.setBounds(14, 10, 550, 20);
        detailHeader.setFont(new Font("Segoe UI", Font.BOLD, 12));
        detailHeader.setForeground(COFFEE);
        detailBox.add(detailHeader);

        JTextArea detailDescArea = new JTextArea("Select a complaint row above to read the full description and worker notes.");
        detailDescArea.setEditable(false);
        detailDescArea.setLineWrap(true);
        detailDescArea.setWrapStyleWord(true);
        detailDescArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        detailDescArea.setBackground(Color.WHITE);
        JScrollPane descPane = new JScrollPane(detailDescArea);
        descPane.setBounds(14, 32, 552, 152);
        descPane.setBorder(BorderFactory.createLineBorder(LINE, 1));
        detailBox.add(descPane);

        complaintsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = complaintsTable.getSelectedRow();
                if (row >= 0) {
                    int cId = (int) complaintsModel.getValueAt(row, 0);
                    String title = (String) complaintsModel.getValueAt(row, 1);
                    String cat = (String) complaintsModel.getValueAt(row, 2);
                    String worker = (String) complaintsModel.getValueAt(row, 3);
                    String desc = (String) complaintsModel.getValueAt(row, 6);
                    detailHeader.setText("Details for Complaint #" + cId + " — " + title + " (" + cat + ") · Worker: " + worker);
                    detailDescArea.setText(desc != null && !desc.isBlank() ? desc : "No description entered for this complaint.");
                } else {
                    detailHeader.setText("Complaint Description & Issue Details");
                    detailDescArea.setText("Select a complaint row above to read the full description and worker notes.");
                }
            }
        });

        return panel;
    }

    // ==========================================
    // 3. PAYMENTS VIEW
    // ==========================================
    private JPanel createPaymentsPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(CREAM);

        // Top Header matching COFFEE theme (no close button)
        panel.add(createTopBar("Maintenance Fee & Payments"));

        // 3 Summary Stat Cards
        JPanel card1 = createSummaryCard("Total Unpaid Due", "₹0", new Color(254, 242, 242), new Color(254, 202, 202), new Color(185, 28, 28), 30, 85, 275, 85);
        totalDuesLbl = (JLabel) card1.getComponent(1);
        panel.add(card1);

        JPanel card2 = createSummaryCard("Overdue Amount", "₹0", new Color(254, 226, 226), new Color(252, 165, 165), new Color(220, 38, 38), 340, 85, 275, 85);
        overdueDuesLbl = (JLabel) card2.getComponent(1);
        panel.add(card2);

        JPanel card3 = createSummaryCard("Paid Bills", "0", new Color(236, 253, 245), new Color(167, 243, 208), new Color(5, 150, 105), 650, 85, 275, 85);
        paidCountLbl = (JLabel) card3.getComponent(1);
        panel.add(card3);

        // Payments Table Panel
        JPanel tablePanel = new JPanel(null);
        tablePanel.setBounds(30, 190, 895, 515);
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createLineBorder(LINE, 1));
        panel.add(tablePanel);

        JLabel panelTitle = new JLabel("Payment History & Due Dates");
        panelTitle.setBounds(20, 15, 300, 25);
        panelTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        panelTitle.setForeground(COFFEE);
        tablePanel.add(panelTitle);

        JButton payBtn = new JButton("Pay Selected Bill");
        payBtn.setBounds(560, 14, 180, 34);
        payBtn.setBackground(new Color(40, 135, 75));
        payBtn.setForeground(Color.WHITE);
        payBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        payBtn.setFocusPainted(false);
        payBtn.setBorder(BorderFactory.createLineBorder(new Color(30, 110, 60), 1));
        payBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        payBtn.addActionListener(e -> processPayment());
        tablePanel.add(payBtn);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBounds(755, 14, 120, 34);
        refreshBtn.setBackground(new Color(242, 231, 215));
        refreshBtn.setForeground(COFFEE);
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setBorder(BorderFactory.createLineBorder(LINE, 1));
        refreshBtn.setFocusPainted(false);
        refreshBtn.addActionListener(e -> loadPayments());
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

        paymentsTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        paymentsTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        paymentsTable.getColumnModel().getColumn(2).setPreferredWidth(105);
        paymentsTable.getColumnModel().getColumn(3).setPreferredWidth(95);
        paymentsTable.getColumnModel().getColumn(4).setPreferredWidth(95);
        paymentsTable.getColumnModel().getColumn(5).setPreferredWidth(115);
        paymentsTable.getColumnModel().getColumn(6).setPreferredWidth(110);
        paymentsTable.getColumnModel().getColumn(7).setPreferredWidth(150);

        paymentsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                Object trackingVal = table.getValueAt(row, 7);

                if (!isSelected) {
                    if (trackingVal != null && trackingVal.toString().contains("OVERDUE")) {
                        c.setForeground(new Color(195, 35, 35));
                        if (col == 6 || col == 7) c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if (trackingVal != null && trackingVal.toString().contains("PAID")) {
                        c.setForeground(new Color(24, 134, 75));
                        if (col == 6 || col == 7) c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setForeground(COFFEE);
                        c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(paymentsTable);
        scrollPane.setBounds(20, 60, 855, 435);
        scrollPane.setBorder(BorderFactory.createLineBorder(LINE, 1));
        tablePanel.add(scrollPane);

        return panel;
    }

    private JPanel createSummaryCard(String title, String value, Color bg, Color borderCol, Color textCol, int x, int y, int width, int height) {
        JPanel card = new JPanel(null);
        card.setBounds(x, y, width, height);
        card.setBackground(bg);
        card.setBorder(BorderFactory.createLineBorder(borderCol, 1));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setBounds(20, 14, width - 30, 20);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(new Color(115, 82, 56));
        card.add(titleLbl);

        JLabel valLbl = new JLabel(value);
        valLbl.setBounds(20, 38, width - 30, 36);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valLbl.setForeground(textCol);
        card.add(valLbl);

        return card;
    }

    // ==========================================
    // DATABASE METHODS
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
            String visitorSql = "SELECT visitor_id, visitor_name, phone, COALESCE(parking_slot, '—') AS slot, status, valid_until " +
                                "FROM visitors WHERE resident_id = ? ORDER BY visitor_id DESC";
            try (PreparedStatement ps = con.prepareStatement(visitorSql)) {
                ps.setInt(1, residentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String st = rs.getString("status");
                        if (st != null) st = st.replace("_", " ");
                        visitorModel.addRow(new Object[] {
                            rs.getInt("visitor_id"),
                            rs.getString("visitor_name"),
                            rs.getString("phone"),
                            rs.getString("slot"),
                            st,
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

    // Lodges a new complaint directly from the form
    private void submitComplaint() {
        String title = complaintTitleField.getText().trim();
        String category = (String) complaintCategoryCombo.getSelectedItem();
        String description = complaintDescArea.getText().trim();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a complaint title.", "Input Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Database connection not available. Please verify your connection.", "Connection Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Integer autoWorkerId = AdminDashboard.findWorkerForComplaint(con, category);

            String sql;
            if (autoWorkerId != null) {
                sql = "INSERT INTO complaints (resident_id, title, description, category, status, worker_id) VALUES (?, ?, ?, ?, 'ASSIGNED', ?)";
            } else {
                sql = "INSERT INTO complaints (resident_id, title, description, category, status) VALUES (?, ?, ?, ?, 'PENDING')";
            }

            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, residentId);
                ps.setString(2, title);
                ps.setString(3, description);
                ps.setString(4, category);
                if (autoWorkerId != null) {
                    ps.setInt(5, autoWorkerId);
                }

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    int generatedId = -1;
                    try (ResultSet gk = ps.getGeneratedKeys()) {
                        if (gk.next()) {
                            generatedId = gk.getInt(1);
                        }
                    }

                    String notice = autoWorkerId != null
                            ? "Complaint #" + generatedId + " submitted successfully!\nAutomatically assigned to an available technician."
                            : "Complaint #" + generatedId + " submitted successfully!\nWill be assigned as soon as a technician becomes available.";
                    JOptionPane.showMessageDialog(this, notice, "Complaint Registered", JOptionPane.INFORMATION_MESSAGE);
                    complaintTitleField.setText("");
                    complaintDescArea.setText("");
                    loadComplaints();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error submitting complaint: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Loads complaints list sorted by ID
    private void loadComplaints() {
        complaintsModel.setRowCount(0);

        String sql = "SELECT c.complaint_id, c.title, c.category, c.status, c.description, " +
                     "c.created_at::DATE AS log_date, " +
                     "COALESCE(w.full_name, 'Unassigned') AS worker_name " +
                     "FROM complaints c " +
                     "LEFT JOIN workers w ON c.worker_id = w.worker_id " +
                     "WHERE c.resident_id = ? " +
                     "ORDER BY c.complaint_id DESC";

        int totalCount = 0;
        int inProgCount = 0;
        int resolvedCount = 0;
        int pendingCount = 0;

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                if (complaintsSummaryLabel != null) complaintsSummaryLabel.setText("Database connection unavailable.");
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, residentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("complaint_id");
                        String title = rs.getString("title");
                        String category = rs.getString("category");
                        String worker = rs.getString("worker_name");
                        String desc = rs.getString("description");
                        java.sql.Date logDate = rs.getDate("log_date");
                        String rawStatus = rs.getString("status");
                        String displayStatus = rawStatus != null ? rawStatus.replace("_", " ") : "PENDING";

                        totalCount++;
                        if ("RESOLVED".equalsIgnoreCase(rawStatus) || "COMPLETED".equalsIgnoreCase(rawStatus)) {
                            resolvedCount++;
                        } else if ("IN_PROGRESS".equalsIgnoreCase(rawStatus) || "IN PROGRESS".equalsIgnoreCase(rawStatus)) {
                            inProgCount++;
                        } else {
                            pendingCount++;
                        }

                        complaintsModel.addRow(new Object[]{
                                id,
                                title,
                                category,
                                worker,
                                displayStatus,
                                logDate != null ? logDate.toString() : "-",
                                desc
                        });
                    }
                }
            }

            if (complaintsSummaryLabel != null) {
                complaintsSummaryLabel.setText(String.format("Total: %d | Resolved: %d | In Progress: %d | Pending: %d", totalCount, resolvedCount, inProgCount, pendingCount));
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (complaintsSummaryLabel != null) {
                complaintsSummaryLabel.setText("Failed to load complaints: " + e.getMessage());
            }
        }
    }

    // Loads payments list and updates stat cards
    private void loadPayments() {
        paymentsModel.setRowCount(0);

        String sql = "SELECT payment_id, month_year, amount, due_date, payment_date, payment_method, status " +
                     "FROM payments WHERE resident_id = ? ORDER BY payment_id DESC";

        double totalPendingAmount = 0.0;
        double overdueAmount = 0.0;
        int paidCount = 0;

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, residentId);
                try (ResultSet rs = ps.executeQuery()) {
                    LocalDate today = LocalDate.now();

                    while (rs.next()) {
                        int id = rs.getInt("payment_id");
                        String monthYear = rs.getString("month_year");
                        double amount = rs.getDouble("amount");
                        java.sql.Date dueDate = rs.getDate("due_date");
                        java.sql.Date paymentDate = rs.getDate("payment_date");
                        String method = rs.getString("payment_method");
                        String status = rs.getString("status");

                        String tracking;
                        boolean isPaid = "PAID".equalsIgnoreCase(status);

                        if (isPaid) {
                            tracking = "PAID";
                            paidCount++;
                        } else {
                            totalPendingAmount += amount;
                            if (dueDate != null && today.isAfter(dueDate.toLocalDate())) {
                                tracking = "⚠️ OVERDUE";
                                overdueAmount += amount;
                            } else {
                                tracking = "Upcoming";
                            }
                        }

                        paymentsModel.addRow(new Object[]{
                                id,
                                monthYear,
                                String.format("%.2f", amount),
                                dueDate != null ? dueDate.toString() : "-",
                                paymentDate != null ? paymentDate.toString() : "-",
                                method != null ? method : "-",
                                status != null ? status : "PENDING",
                                tracking
                        });
                    }
                }
            }

            if (totalDuesLbl != null) totalDuesLbl.setText(String.format("₹%.0f", totalPendingAmount));
            if (overdueDuesLbl != null) overdueDuesLbl.setText(String.format("₹%.0f", overdueAmount));
            if (paidCountLbl != null) paidCountLbl.setText(String.valueOf(paidCount));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Handles bill payment with method selection
    private void processPayment() {
        int selectedRow = paymentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an unpaid bill from the table to pay.", "Select Bill", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int paymentId = (int) paymentsModel.getValueAt(selectedRow, 0);
        String monthYear = (String) paymentsModel.getValueAt(selectedRow, 1);
        String amountStr = (String) paymentsModel.getValueAt(selectedRow, 2);
        String currentStatus = (String) paymentsModel.getValueAt(selectedRow, 6);

        if ("PAID".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "This bill (" + monthYear + ") has already been paid.", "Already Paid", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] options = {"UPI", "Credit/Debit Card", "Net Banking", "Cash"};
        String method = (String) JOptionPane.showInputDialog(
                this,
                "Select Payment Method for " + monthYear + " (₹" + amountStr + "):",
                "Complete Payment",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (method == null) return; // Cancelled

        String updateSql = "UPDATE payments SET status = 'PAID', payment_date = CURRENT_DATE, payment_method = ? WHERE payment_id = ?";

        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed. Unable to record payment.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                ps.setString(1, method);
                ps.setInt(2, paymentId);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this,
                            "Payment Successful!\n\n" +
                            "Month: " + monthYear + "\n" +
                            "Amount: ₹" + amountStr + "\n" +
                            "Method: " + method,
                            "Payment Receipt",
                            JOptionPane.INFORMATION_MESSAGE);

                    loadPayments();
                    loadDashboardData();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error processing payment: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==========================================
    // HELPER UI METHODS
    // ==========================================
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
        return new SwingNavIcon(type);
    }

    // Custom Icon implementation using Java Swing Icon interface
    private static class SwingNavIcon implements Icon {
        private final String type;
        private final int size = 18;

        public SwingNavIcon(String type) {
            this.type = type;
        }

        @Override
        public int getIconWidth() { return size; }

        @Override
        public int getIconHeight() { return size; }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(CREAM);

            if ("OVERVIEW".equals(type)) {
                // 4-tile dashboard grid
                g2.fillRoundRect(x, y, 7, 7, 2, 2);
                g2.fillRoundRect(x + 9, y, 7, 7, 2, 2);
                g2.fillRoundRect(x, y + 9, 7, 7, 2, 2);
                g2.fillRoundRect(x + 9, y + 9, 7, 7, 2, 2);
            } else if ("COMPLAINTS".equals(type)) {
                // Clipboard with checklist lines
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(x + 2, y + 3, 14, 13, 3, 3);
                g2.fillRect(x + 6, y + 1, 6, 3);
                g2.drawLine(x + 5, y + 7, x + 13, y + 7);
                g2.drawLine(x + 5, y + 10, x + 13, y + 10);
            } else if ("PAYMENTS".equals(type)) {
                // Credit card with magnetic stripe and chip
                g2.setStroke(new BasicStroke(1.8f));
                g2.drawRoundRect(x + 1, y + 2, 16, 13, 3, 3);
                g2.fillRect(x + 1, y + 5, 16, 3);
                g2.fillRect(x + 3, y + 10, 4, 3);
            } else if ("LOGOUT".equals(type)) {
                // Door outline and exit arrow
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
    }

    private JPanel createCard(String title, JLabel valueLabel) {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(new Color(242, 231, 215));
        p.setBorder(BorderFactory.createLineBorder(new Color(233, 218, 197), 1));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setForeground(new Color(115, 82, 56));
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));

        valueLabel.setForeground(COFFEE);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));

        p.add(titleLbl, BorderLayout.NORTH);
        p.add(valueLabel, BorderLayout.CENTER);
        return p;
    }

    private void styleOverviewTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(242, 235, 226));
        table.setSelectionBackground(new Color(242, 225, 201));
        table.setSelectionForeground(COFFEE);
        table.getTableHeader().setBackground(new Color(239, 226, 209));
        table.getTableHeader().setForeground(COFFEE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
    }
}
