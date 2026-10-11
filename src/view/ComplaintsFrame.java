package view;

import database.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ComplaintsFrame extends JFrame {
    private final int residentId;
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] { "ID", "Title", "Category", "Status", "Created" }, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    public ComplaintsFrame(int residentId) {
        this.residentId = residentId;
        setTitle("Maintenance complaints");
        setSize(780, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        panel.setBackground(new Color(250, 245, 235));
        setContentPane(panel);

        JLabel title = new JLabel("Maintenance & Complaints");
        title.setFont(new Font("Serif", Font.BOLD, 25));
        panel.add(title, BorderLayout.NORTH);

        JTable table = new JTable(model);
        styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        JButton addBtn = LoginFrame.button("NEW COMPLAINT");
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFocusPainted(false);
        bottom.add(refreshBtn);
        bottom.add(addBtn);
        panel.add(bottom, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> createComplaint());
        refreshBtn.addActionListener(e -> loadComplaints());

        // Load complaints from database on open
        loadComplaints();
        setVisible(true);
    }

    private void styleTable(JTable t) {
        t.setRowHeight(28);
        t.setFillsViewportHeight(true);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(242, 235, 226));
        t.setSelectionBackground(new Color(242, 225, 201));
        t.setSelectionForeground(new Color(55, 36, 27));
        t.getTableHeader().setBackground(new Color(239, 226, 209));
        t.getTableHeader().setForeground(new Color(55, 36, 27));
        t.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
    }

    // Opens input form dialog to submit a new complaint
    private void createComplaint() {
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

        int result = JOptionPane.showConfirmDialog(this, form, "Submit complaint", JOptionPane.OK_CANCEL_OPTION);
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

        String sql = "INSERT INTO \"A-partment\".complaints(resident_id, title, description, category, status) VALUES(?, ?, ?, ?, 'PENDING')";
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

    // Fetches complaints for this resident and populates the table
    private void loadComplaints() {
        model.setRowCount(0);
        String sql = "SELECT complaint_id, title, category, status, created_at FROM \"A-partment\".complaints WHERE resident_id = ? ORDER BY complaint_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, residentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[] {
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
}
