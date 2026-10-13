package view;

import controller.WorkerController;
import model.Complaint;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

public class WorkerDashboard extends JFrame {
    private final int workerId;
    private final String workerName;
    private final WorkerController workerController = new WorkerController();

    // Table model for maintenance complaints
    // Columns: ID, Resident, Apartment, Title, Category, Status, Assigned Worker, Description (hidden), Worker ID (hidden)
    private final DefaultTableModel complaintsModel = new DefaultTableModel(
            new String[] { "ID", "Resident", "Apartment", "Title", "Category", "Status", "Assigned Worker", "Description", "Worker ID" }, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    private final JLabel statusLabel = new JLabel("Loading...");
    private final JLabel detailHeader = new JLabel("Work Description & Issue Details (Select a complaint row to view)");
    private final JTextArea detailDescArea = new JTextArea("Select a complaint row above to read the resident's issue description.");
    private final JButton claimBtn;
    private final JButton progressBtn;
    private final JButton completeBtn;
    private final JTable complaintsTable;

    private static final Color COFFEE = new Color(55, 36, 27);
    private static final Color CREAM = new Color(250, 245, 235);
    private static final Color LINE = new Color(229, 216, 199);

    public WorkerDashboard(int id, String name) {
        this.workerId = id;
        this.workerName = name;

        setTitle("A-partment Helper | Worker Desk");
        setSize(1180, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(18, 16));
        root.setBackground(CREAM);
        root.setBorder(BorderFactory.createEmptyBorder(22, 28, 20, 28));
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

        JLabel sub = new JLabel("Welcome, " + workerName + "  ·  Technician ID: #" + workerId + "  ·  Maintenance complaints overview");
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

        // Center: Complaints Table + Issue Description Panel
        complaintsTable = new JTable(complaintsModel);
        styleTable(complaintsTable);

        // Hide column 8 (Worker ID) and column 7 (Description) from direct table display
        complaintsTable.removeColumn(complaintsTable.getColumnModel().getColumn(8));
        complaintsTable.removeColumn(complaintsTable.getColumnModel().getColumn(7));

        complaintsTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        complaintsTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        complaintsTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        complaintsTable.getColumnModel().getColumn(3).setPreferredWidth(210);
        complaintsTable.getColumnModel().getColumn(4).setPreferredWidth(110);
        complaintsTable.getColumnModel().getColumn(5).setPreferredWidth(110);
        complaintsTable.getColumnModel().getColumn(6).setPreferredWidth(160);

        // Description / Issue Details Box
        JPanel detailBox = new JPanel(new BorderLayout(0, 6));
        detailBox.setBackground(new Color(253, 250, 246));
        detailBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE, 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        detailBox.setPreferredSize(new Dimension(100, 155));

        detailHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        detailHeader.setForeground(COFFEE);
        detailBox.add(detailHeader, BorderLayout.NORTH);

        detailDescArea.setEditable(false);
        detailDescArea.setLineWrap(true);
        detailDescArea.setWrapStyleWord(true);
        detailDescArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        detailDescArea.setBackground(Color.WHITE);
        JScrollPane descScroll = new JScrollPane(detailDescArea);
        descScroll.setBorder(BorderFactory.createLineBorder(LINE, 1));
        detailBox.add(descScroll, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);
        centerPanel.add(new JScrollPane(complaintsTable), BorderLayout.CENTER);
        centerPanel.add(detailBox, BorderLayout.SOUTH);

        JPanel complaintsPanel = createSection("Maintenance complaints", centerPanel);
        root.add(complaintsPanel, BorderLayout.CENTER);

        // Bottom Actions Bar
        JPanel actions = new JPanel(new BorderLayout());
        actions.setOpaque(false);
        statusLabel.setForeground(new Color(126, 105, 86));
        actions.add(statusLabel, BorderLayout.WEST);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        claimBtn = LoginFrame.button("CLAIM SELECTED");
        progressBtn = LoginFrame.button("MARK IN PROGRESS");
        completeBtn = LoginFrame.button("MARK COMPLETED");

        // Initially disabled until row selection
        claimBtn.setEnabled(false);
        progressBtn.setEnabled(false);
        completeBtn.setEnabled(false);

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

        // Row Selection Listener: update description and button states
        complaintsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectionState();
            }
        });

        // Load initial data
        loadData();
        setVisible(true);
    }

    private void updateSelectionState() {
        int selectedRow = complaintsTable.getSelectedRow();
        if (selectedRow < 0) {
            claimBtn.setEnabled(false);
            progressBtn.setEnabled(false);
            completeBtn.setEnabled(false);
            detailHeader.setText("Work Description & Issue Details (Select a complaint row above to view)");
            detailDescArea.setText("Select a complaint row to view the resident's issue description.");
            return;
        }

        int modelRow = complaintsTable.convertRowIndexToModel(selectedRow);
        int complaintId = (int) complaintsModel.getValueAt(modelRow, 0);
        String resName = (String) complaintsModel.getValueAt(modelRow, 1);
        String apt = (String) complaintsModel.getValueAt(modelRow, 2);
        String title = (String) complaintsModel.getValueAt(modelRow, 3);
        String cat = (String) complaintsModel.getValueAt(modelRow, 4);
        String status = String.valueOf(complaintsModel.getValueAt(modelRow, 5)).trim();
        String assignedWorkerName = (String) complaintsModel.getValueAt(modelRow, 6);
        String desc = (String) complaintsModel.getValueAt(modelRow, 7);
        Integer assignedWorkerId = (Integer) complaintsModel.getValueAt(modelRow, 8);

        boolean isMyWork = (assignedWorkerId != null && assignedWorkerId == this.workerId);
        boolean isUnassigned = (assignedWorkerId == null || assignedWorkerId == 0 || "PENDING".equalsIgnoreCase(status) || "Unassigned".equalsIgnoreCase(assignedWorkerName));
        boolean isCompleted = "COMPLETED".equalsIgnoreCase(status) || "RESOLVED".equalsIgnoreCase(status);
        boolean isInProgress = "IN PROGRESS".equalsIgnoreCase(status) || "IN_PROGRESS".equalsIgnoreCase(status);
        boolean isAssigned = "ASSIGNED".equalsIgnoreCase(status);

        String ownershipTag = isMyWork
                ? " [✓ Assigned to YOU]"
                : (isUnassigned ? " [⚠️ Unassigned]" : " [Assigned to: " + assignedWorkerName + "]");

        detailHeader.setText("Complaint #" + complaintId + " — " + title + " (" + cat + ") | Resident: " + resName + " (" + apt + ") · Status: " + status + ownershipTag);
        detailDescArea.setText(desc != null && !desc.isBlank() ? desc : "No description provided for this complaint.");

        // 1. Claim button: enabled as long as it is not in progress, not completed, and not already assigned to this worker
        claimBtn.setEnabled(!isMyWork && !isInProgress && !isCompleted);

        // 2. Mark in progress: enabled ONLY if assigned to the logged-in worker, currently ASSIGNED, and not completed
        progressBtn.setEnabled(isMyWork && isAssigned && !isCompleted);

        // 3. Mark completed: enabled ONLY if assigned to the logged-in worker, and in progress (or assigned), and not completed
        completeBtn.setEnabled(isMyWork && (isInProgress || isAssigned) && !isCompleted);
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

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                Object statusVal = table.getValueAt(row, 5);

                if (!isSelected) {
                    if (statusVal != null && (statusVal.toString().equalsIgnoreCase("COMPLETED") || statusVal.toString().equalsIgnoreCase("RESOLVED"))) {
                        if (col == 5) {
                            c.setForeground(new Color(24, 134, 75));
                            c.setFont(c.getFont().deriveFont(Font.BOLD));
                        } else {
                            c.setForeground(COFFEE);
                            c.setFont(c.getFont().deriveFont(Font.PLAIN));
                        }
                    } else if (statusVal != null && statusVal.toString().toUpperCase().contains("IN PROGRESS")) {
                        if (col == 5) {
                            c.setForeground(new Color(205, 115, 25));
                            c.setFont(c.getFont().deriveFont(Font.BOLD));
                        } else {
                            c.setForeground(COFFEE);
                            c.setFont(c.getFont().deriveFont(Font.PLAIN));
                        }
                    } else if (statusVal != null && statusVal.toString().toUpperCase().contains("ASSIGNED")) {
                        if (col == 5) {
                            c.setForeground(new Color(29, 111, 173));
                            c.setFont(c.getFont().deriveFont(Font.BOLD));
                        } else {
                            c.setForeground(COFFEE);
                            c.setFont(c.getFont().deriveFont(Font.PLAIN));
                        }
                    } else {
                        if (col == 5) {
                            c.setForeground(new Color(185, 28, 28));
                            c.setFont(c.getFont().deriveFont(Font.BOLD));
                        } else {
                            c.setForeground(COFFEE);
                            c.setFont(c.getFont().deriveFont(Font.PLAIN));
                        }
                    }
                }
                return c;
            }
        });
    }

    // Fetches complaints via WorkerController
    private void loadData() {
        statusLabel.setText("Refreshing data...");
        try {
            complaintsModel.setRowCount(0);
            List<Complaint> complaints = workerController.getAllComplaints();

            for (Complaint c : complaints) {
                String displayStatus = c.getStatus() != null ? c.getStatus().replace("_", " ") : "PENDING";
                complaintsModel.addRow(new Object[] {
                    c.getComplaintId(),
                    c.getResidentName(),
                    c.getApartmentNo(),
                    c.getTitle(),
                    c.getCategory(),
                    displayStatus,
                    c.getWorkerName() != null ? c.getWorkerName() : "Unassigned",
                    c.getDescription() != null ? c.getDescription() : "",
                    c.getWorkerId()
                });
            }

            statusLabel.setText("Updated just now · " + complaintsModel.getRowCount() + " maintenance complaints");
            updateSelectionState();
        } catch (Exception ex) {
            statusLabel.setText("Refresh failed");
            LoginFrame.showDbError(ex);
        }
    }

    // Updates complaint status (claim, in progress, completed) via WorkerController
    private void updateComplaintStatus(JTable table, String newStatus) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a complaint first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        int complaintId = (int) complaintsModel.getValueAt(modelRow, 0);
        String currentStatus = String.valueOf(complaintsModel.getValueAt(modelRow, 5)).trim();
        Integer assignedWorkerId = (Integer) complaintsModel.getValueAt(modelRow, 8);
        String assignedWorkerName = (String) complaintsModel.getValueAt(modelRow, 6);

        if ("COMPLETED".equalsIgnoreCase(currentStatus) || "RESOLVED".equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "This complaint is already completed.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Ownership validation: worker cannot advance work that isn't theirs
        if ("IN_PROGRESS".equals(newStatus) || "COMPLETED".equals(newStatus)) {
            if (assignedWorkerId == null || assignedWorkerId != this.workerId) {
                JOptionPane.showMessageDialog(this,
                        "You cannot update this task.\nIt is assigned to: " + (assignedWorkerName != null ? assignedWorkerName : "another worker") +
                        "\nOnly the assigned technician (" + this.workerName + ") can update or complete this work.",
                        "Not Your Work", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        try {
            boolean success = false;
            if ("ASSIGNED".equals(newStatus)) {
                if ("IN_PROGRESS".equalsIgnoreCase(currentStatus) || "IN PROGRESS".equalsIgnoreCase(currentStatus)) {
                    JOptionPane.showMessageDialog(this, "This work is already in progress and cannot be claimed.", "Task In Progress", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (assignedWorkerId != null && assignedWorkerId == this.workerId) {
                    JOptionPane.showMessageDialog(this, "This task is already assigned to you.", "Already Assigned", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                success = workerController.claimComplaint(complaintId, workerId);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Complaint #" + complaintId + " successfully claimed by you!", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                }
            } else if ("IN_PROGRESS".equals(newStatus)) {
                if (!"ASSIGNED".equalsIgnoreCase(currentStatus)) {
                    JOptionPane.showMessageDialog(this, "Task must be in 'ASSIGNED' state to mark in progress.", "Status Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                success = workerController.markInProgress(complaintId);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Complaint #" + complaintId + " updated to In Progress.", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                }
            } else if ("COMPLETED".equals(newStatus)) {
                String title = (String) complaintsModel.getValueAt(modelRow, 3);
                String resName = (String) complaintsModel.getValueAt(modelRow, 1);
                String apt = (String) complaintsModel.getValueAt(modelRow, 2);

                JTextField amtField = new JTextField("0");
                JTextField noteField = new JTextField("Resolved: " + title);

                JPanel billForm = new JPanel(new GridLayout(0, 1, 6, 6));
                billForm.add(new JLabel("Marking job completed for " + resName + " (" + apt + ")"));
                billForm.add(new JLabel("Bill Amount to charge resident (₹) [0 if included/free]:"));
                billForm.add(amtField);
                billForm.add(new JLabel("Work Completion Note / Bill Description:"));
                billForm.add(noteField);

                int opt = JOptionPane.showConfirmDialog(this, billForm, "Complete Work & Bill Resident", JOptionPane.OK_CANCEL_OPTION);
                if (opt != JOptionPane.OK_OPTION) {
                    return; // Worker cancelled
                }

                double billedAmount = 0.0;
                try {
                    billedAmount = Double.parseDouble(amtField.getText().trim());
                    if (billedAmount < 0) billedAmount = 0;
                } catch (Exception ignored) {
                    billedAmount = 0;
                }
                String note = noteField.getText().trim();

                success = workerController.completeComplaint(complaintId, workerId, note, billedAmount);
                if (success) {
                    if (billedAmount > 0) {
                        JOptionPane.showMessageDialog(this,
                                "Job COMPLETED!\nBill of ₹" + String.format("%.2f", billedAmount) +
                                " automatically generated and sent to resident.",
                                "Success & Billed", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Complaint #" + complaintId + " marked as COMPLETED.", "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }

            if (!success) {
                JOptionPane.showMessageDialog(this, "Could not update complaint. Check if it is assigned to you.", "Update Failed", JOptionPane.WARNING_MESSAGE);
            }
            loadData();
        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
        }
    }
}
