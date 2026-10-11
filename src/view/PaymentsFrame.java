package view;

import database.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class PaymentsFrame extends JFrame {
    private final int residentId;
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] { "ID", "Period", "Amount (₹)", "Due date", "Status", "Paid on" }, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    public PaymentsFrame(int residentId) {
        this.residentId = residentId;
        setTitle("Maintenance payments");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(new Color(250, 245, 235));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        setContentPane(panel);

        JLabel title = new JLabel("Maintenance Payments");
        title.setFont(new Font("Serif", Font.BOLD, 25));
        panel.add(title, BorderLayout.NORTH);

        JTable table = new JTable(model);
        styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setFocusPainted(false);
        JButton recordBtn = LoginFrame.button("RECORD PAYMENT");
        actions.add(refreshBtn);
        actions.add(recordBtn);
        panel.add(actions, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> loadPayments());
        recordBtn.addActionListener(e -> recordPayment(table));

        // Load payments on open
        loadPayments();
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

    // Records payment for a selected bill
    private void recordPayment(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a payment from the table first.");
            return;
        }

        int paymentId = (int) model.getValueAt(selectedRow, 0);
        String currentStatus = String.valueOf(model.getValueAt(selectedRow, 4));

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

        String sql = "UPDATE \"A-partment\".payments SET status = 'PAID', payment_date = CURRENT_DATE, payment_method = ? " +
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
        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
        }
    }

    // Loads payments list for this resident
    private void loadPayments() {
        model.setRowCount(0);
        String sql = "SELECT payment_id, month_year, amount, due_date, status, payment_date " +
                     "FROM \"A-partment\".payments WHERE resident_id = ? ORDER BY due_date DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, residentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[] {
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
}
