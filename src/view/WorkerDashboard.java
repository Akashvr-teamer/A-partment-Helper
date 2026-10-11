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

    // Table model for parking slots
    private final DefaultTableModel parkingModel = new DefaultTableModel(
            new String[] { "Slot", "Type", "Status", "Occupant" }, 0) {
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
        setMinimumSize(new Dimension(950, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

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
        title.setFont(new Font("Serif", Font.BOLD, 30));
        title.setForeground(COFFEE);

        JLabel sub = new JLabel("Welcome, " + workerName + "  ·  Complaints and parking overview");
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

        // Center Tables: Split between Complaints (top) and Parking (bottom)
        JTable complaintsTable = new JTable(complaintsModel);
        styleTable(complaintsTable);
        JPanel complaintsPanel = createSection("Maintenance complaints", new JScrollPane(complaintsTable));

        JTable parkingTable = new JTable(parkingModel);
        styleTable(parkingTable);
        JPanel parkingPanel = createSection("Parking occupancy", new JScrollPane(parkingTable));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, complaintsPanel, parkingPanel);
        splitPane.setResizeWeight(0.65);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        root.add(splitPane, BorderLayout.CENTER);

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
        label.setFont(new Font("Serif", Font.BOLD, 20));
        label.setForeground(COFFEE);
        p.add(label, BorderLayout.NORTH);
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    private void styleTable(JTable t) {
        t.setRowHeight(29);
        t.setFillsViewportHeight(true);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(242, 235, 226));
        t.setSelectionBackground(new Color(242, 225, 201));
        t.setSelectionForeground(COFFEE);
        t.getTableHeader().setBackground(new Color(239, 226, 209));
        t.getTableHeader().setForeground(COFFEE);
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
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
                "FROM \"A-partment\".complaints c " +
                "JOIN \"A-partment\".residents r ON r.resident_id = c.resident_id " +
                "LEFT JOIN \"A-partment\".workers w ON w.worker_id = c.worker_id " +
                "ORDER BY CASE c.status WHEN 'PENDING' THEN 0 WHEN 'ASSIGNED' THEN 1 WHEN 'IN_PROGRESS' THEN 2 ELSE 3 END, c.created_at";

            try (PreparedStatement ps = con.prepareStatement(complaintsSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    complaintsModel.addRow(new Object[] {
                        rs.getInt("complaint_id"),
                        rs.getString("full_name"),
                        rs.getString("apartment_no"),
                        rs.getString("title"),
                        rs.getString("category"),
                        rs.getString("status"),
                        rs.getString("worker")
                    });
                }
            }

            // 2. Fetch parking slots
            parkingModel.setRowCount(0);
            String parkingSql =
                "SELECT p.slot_number, p.parking_type, p.status, " +
                "       COALESCE(r.full_name, v.visitor_name, '—') AS occupant " +
                "FROM \"A-partment\".parking p " +
                "LEFT JOIN \"A-partment\".residents r ON r.resident_id = p.resident_id " +
                "LEFT JOIN \"A-partment\".visitors v ON v.visitor_id = p.visitor_id " +
                "ORDER BY p.parking_type, p.slot_number";

            try (PreparedStatement ps = con.prepareStatement(parkingSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    parkingModel.addRow(new Object[] {
                        rs.getString("slot_number"),
                        rs.getString("parking_type"),
                        rs.getString("status"),
                        rs.getString("occupant")
                    });
                }
            }

            statusLabel.setText("Updated just now · " + complaintsModel.getRowCount() + " complaints · " + parkingModel.getRowCount() + " parking slots");
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

        String sql;
        if ("ASSIGNED".equals(newStatus)) {
            if (!"PENDING".equals(currentStatus) && !"ASSIGNED".equals(currentStatus)) {
                JOptionPane.showMessageDialog(this, "Only pending complaints can be claimed.");
                return;
            }
            sql = "UPDATE \"A-partment\".complaints SET worker_id = ?, status = 'ASSIGNED' WHERE complaint_id = ?";
        } else if ("IN_PROGRESS".equals(newStatus)) {
            if (!"ASSIGNED".equals(currentStatus)) {
                JOptionPane.showMessageDialog(this, "Claim this complaint first before marking in progress.");
                return;
            }
            sql = "UPDATE \"A-partment\".complaints SET status = 'IN_PROGRESS' WHERE complaint_id = ? AND worker_id = ?";
        } else {
            // COMPLETED
            sql = "UPDATE \"A-partment\".complaints SET status = 'COMPLETED', completed_at = CURRENT_TIMESTAMP WHERE complaint_id = ? AND worker_id = ?";
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
                JOptionPane.showMessageDialog(this, "Complaint updated to " + newStatus + ".");
            } else {
                JOptionPane.showMessageDialog(this, "Could not update complaint. Check if it is assigned to you.");
            }
            loadData();
        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
        }
    }
}
