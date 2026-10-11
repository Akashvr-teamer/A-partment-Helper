package view;

import database.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class WorkerDashboard extends JFrame {
    private final int workerId;
    private final String workerName;

    // Table model for maintenance complaints
    private final DefaultTableModel complaintsModel = new DefaultTableModel(
            new String[] { "ID", "Resident", "Apartment", "Title", "Category", "Status", "Assigned Worker" }, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    private final JLabel statusLabel = new JLabel("Loading...");
    private static final Color COFFEE = new Color(55, 36, 27);
    private static final Color CREAM = new Color(250, 245, 235);
    private static final Color LINE = new Color(229, 216, 199);

    public WorkerDashboard(int id, String name) {
        this.workerId = id;
        this.workerName = name;

        setTitle("A-partment Helper | Worker Desk");
        setSize(1180, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBackground(CREAM);
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 22, 28));
        setContentPane(root);

        // Header Section
        JPanel head = new JPanel(new BorderLayout(10, 8));
        head.setOpaque(false);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Worker Desk");
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(COFFEE);

        JLabel sub = new JLabel("Welcome, " + workerName + "  ·  Maintenance complaints overview");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(126, 105, 86));

        heading.add(title);
        heading.add(Box.createVerticalStrut(5));
        heading.add(sub);
        head.add(heading, BorderLayout.WEST);

        JPanel headRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        headRight.setOpaque(false);
        JButton refreshBtn = LoginFrame.button("REFRESH");
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFocusPainted(false);
        headRight.add(refreshBtn);
        headRight.add(logoutBtn);
        head.add(headRight, BorderLayout.EAST);
        root.add(head, BorderLayout.NORTH);

        // Center Table: Complaints occupying full height
        JTable complaintsTable = new JTable(complaintsModel);
        styleTable(complaintsTable);
        JPanel complaintsPanel = createSection("Maintenance complaints", new JScrollPane(complaintsTable));
        root.add(complaintsPanel, BorderLayout.CENTER);

        // Bottom Actions Bar
        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        statusLabel.setForeground(new Color(126, 105, 86));
        actions.add(statusLabel, BorderLayout.WEST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        JButton claimBtn = LoginFrame.button("CLAIM SELECTED");
        JButton progressBtn = LoginFrame.button("MARK IN PROGRESS");
        JButton completeBtn = LoginFrame.button("MARK COMPLETED");
        buttons.add(claimBtn);
        buttons.add(progressBtn);
        buttons.add(completeBtn);
        actions.add(buttons, BorderLayout.EAST);
        root.add(actions, BorderLayout.SOUTH);

        // Event Listeners
        refreshBtn.addActionListener(e -> loadData());
        logoutBtn.addActionListener(e -> {
            new WelcomeFrame();
            dispose();
        });

        claimBtn.addActionListener(e -> updateComplaintStatus(complaintsTable, "ASSIGNED"));
        progressBtn.addActionListener(e -> updateComplaintStatus(complaintsTable, "IN_PROGRESS"));
        completeBtn.addActionListener(e -> updateComplaintStatus(complaintsTable, "COMPLETED"));

        // Load initial data
        loadData();
        setVisible(true);
    }

    private JPanel createSection(String title, JComponent content) {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));

        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(COFFEE);
        p.add(label, BorderLayout.NORTH);
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    private void styleTable(JTable t) {
        t.setRowHeight(29);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setFillsViewportHeight(true);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(242, 235, 226));
        t.setSelectionBackground(new Color(242, 225, 201));
        t.setSelectionForeground(COFFEE);
        t.getTableHeader().setBackground(new Color(239, 226, 209));
        t.getTableHeader().setForeground(COFFEE);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
    }

    // Fetches complaints and parking data from database
    private void loadData() {
        statusLabel.setText("Refreshing data...");
        try (Connection con = DBConnection.getConnection()) {

            // 1. Fetch complaints
            complaintsModel.setRowCount(0);
            String complaintsSql =
                "SELECT c.complaint_id, r.full_name, r.apartment_no, c.title, c.category, c.status, " +
                "       COALESCE(w.full_name, 'Unassigned') AS worker " +
                "FROM complaints c " +
                "JOIN residents r ON r.resident_id = c.resident_id " +
                "LEFT JOIN workers w ON w.worker_id = c.worker_id " +
                "ORDER BY CASE c.status WHEN 'PENDING' THEN 0 WHEN 'ASSIGNED' THEN 1 WHEN 'IN_PROGRESS' THEN 2 ELSE 3 END, c.created_at";

            try (PreparedStatement ps = con.prepareStatement(complaintsSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String displayStatus = rs.getString("status");
                    if (displayStatus != null) {
                        displayStatus = displayStatus.replace("_", " ");
                    }
                    complaintsModel.addRow(new Object[] {
                        rs.getInt("complaint_id"),
                        rs.getString("full_name"),
                        rs.getString("apartment_no"),
                        rs.getString("title"),
                        rs.getString("category"),
                        displayStatus,
                        rs.getString("worker")
                    });
                }
            }

            statusLabel.setText("Updated just now · " + complaintsModel.getRowCount() + " maintenance complaints");
        } catch (Exception ex) {
            statusLabel.setText("Refresh failed");
            LoginFrame.showDbError(ex);
        }
    }

    // Updates complaint status (claim, in progress, completed)
    private void updateComplaintStatus(JTable table, String newStatus) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a complaint first.");
            return;
        }

        int complaintId = (int) complaintsModel.getValueAt(selectedRow, 0);
        String currentStatus = String.valueOf(complaintsModel.getValueAt(selectedRow, 5));

        if ("COMPLETED".equals(currentStatus)) {
            JOptionPane.showMessageDialog(this, "This complaint is already completed.");
            return;
        }

        double billedAmount = 0.0;
        String billDesc = null;

        if ("COMPLETED".equals(newStatus)) {
            String title = (String) complaintsModel.getValueAt(selectedRow, 3);
            String resName = (String) complaintsModel.getValueAt(selectedRow, 1);
            String apt = (String) complaintsModel.getValueAt(selectedRow, 2);

            JTextField amtField = new JTextField("0");
            JTextField descField = new JTextField("Maintenance Service: " + title);

            JPanel billForm = new JPanel(new GridLayout(0, 1, 6, 6));
            billForm.add(new JLabel("Marking job completed for " + resName + " (" + apt + ")"));
            billForm.add(new JLabel("Bill Amount to charge resident (₹) [0 if included/free]:"));
            billForm.add(amtField);
            billForm.add(new JLabel("Bill Description / Notes:"));
            billForm.add(descField);

            int opt = JOptionPane.showConfirmDialog(this, billForm, "Complete Work & Bill Resident", JOptionPane.OK_CANCEL_OPTION);
            if (opt != JOptionPane.OK_OPTION) {
                return; // Worker cancelled
            }

            try {
                billedAmount = Double.parseDouble(amtField.getText().trim());
                if (billedAmount < 0) billedAmount = 0;
            } catch (Exception ignored) {
                billedAmount = 0;
            }
            billDesc = descField.getText().trim();
            if (billDesc.isEmpty()) {
                billDesc = "Maintenance Service: " + title;
            }
            if (billDesc.length() > 255) {
                billDesc = billDesc.substring(0, 255);
            }
        }

        String sql;
        if ("ASSIGNED".equals(newStatus)) {
            if (!"PENDING".equals(currentStatus) && !"ASSIGNED".equals(currentStatus)) {
                JOptionPane.showMessageDialog(this, "Only pending complaints can be claimed.");
                return;
            }
            sql = "UPDATE complaints SET worker_id = ?, status = 'ASSIGNED' WHERE complaint_id = ?";
        } else if ("IN_PROGRESS".equals(newStatus)) {
            if (!"ASSIGNED".equals(currentStatus)) {
                JOptionPane.showMessageDialog(this, "Claim this complaint first before marking in progress.");
                return;
            }
            sql = "UPDATE complaints SET status = 'IN_PROGRESS' WHERE complaint_id = ? AND worker_id = ?";
        } else {
            // COMPLETED
            sql = "UPDATE complaints SET status = 'COMPLETED', completed_at = CURRENT_TIMESTAMP WHERE complaint_id = ? AND worker_id = ?";
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if ("ASSIGNED".equals(newStatus)) {
                ps.setInt(1, workerId);
                ps.setInt(2, complaintId);
            } else {
                ps.setInt(1, complaintId);
                ps.setInt(2, workerId);
            }

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                if ("COMPLETED".equals(newStatus) && billedAmount > 0) {
                    // Fetch resident_id for this complaint
                    int residentId = -1;
                    String getResSql = "SELECT resident_id FROM complaints WHERE complaint_id = ?";
                    try (PreparedStatement psRes = con.prepareStatement(getResSql)) {
                        psRes.setInt(1, complaintId);
                        try (ResultSet rsRes = psRes.executeQuery()) {
                            if (rsRes.next()) {
                                residentId = rsRes.getInt("resident_id");
                            }
                        }
                    }

                    if (residentId != -1) {
                        String billSql = "INSERT INTO payments (resident_id, month_year, amount, due_date, status, payment_type, complaint_id) " +
                                         "VALUES (?, ?, ?, CURRENT_DATE + INTERVAL '7 days', 'PENDING', 'WORKER_SERVICE', ?)";
                        try (PreparedStatement psBill = con.prepareStatement(billSql)) {
                            psBill.setInt(1, residentId);
                            psBill.setString(2, billDesc);
                            psBill.setDouble(3, billedAmount);
                            psBill.setInt(4, complaintId);
                            psBill.executeUpdate();
                        }
                    }

                    JOptionPane.showMessageDialog(this,
                            "Job COMPLETED!\nBill of ₹" + String.format("%.2f", billedAmount) +
                            " automatically generated and sent to resident.",
                            "Success & Billed", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Complaint updated to " + newStatus.replace("_", " ") + ".");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Could not update complaint. Check if it is assigned to you.");
            }
            loadData();
        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
        }
    }
}
