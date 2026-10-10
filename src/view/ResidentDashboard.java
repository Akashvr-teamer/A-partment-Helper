package view;

import database.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ResidentDashboard extends JFrame {
    private final int residentId;
    private final String residentName;
    private final JLabel parkingVal = new JLabel("—"), visitorsVal = new JLabel("0"), maintenanceVal = new JLabel("₹0");
    private final JLabel loadState = new JLabel(" ");
    private final DefaultTableModel visitorModel = new DefaultTableModel(new String[]{"Visitor", "Phone", "Parking", "Status", "Valid Until"}, 0) {
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private static final Color COFFEE = new Color(55, 36, 27), AMBER = new Color(190, 124, 58), CREAM = new Color(250, 245, 235), INK = new Color(65, 52, 43), LINE = new Color(229, 216, 199);

    public ResidentDashboard(int id, String name) {
        residentId = id; residentName = name;
        setTitle("A-partment Helper | Resident Dashboard"); setSize(1180, 760); setMinimumSize(new Dimension(940, 650)); setLocationRelativeTo(null); setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout()); root.setBackground(CREAM); setContentPane(root);
        JPanel side = new JPanel(); side.setPreferredSize(new Dimension(205, 10)); side.setBackground(COFFEE); side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS)); root.add(side, BorderLayout.WEST);
        JLabel logo = new JLabel("  A-partment"); logo.setForeground(CREAM); logo.setFont(new Font("Serif", Font.BOLD, 24)); logo.setBorder(BorderFactory.createEmptyBorder(26, 10, 34, 5)); side.add(logo);
        side.add(menu("Overview", this::loadDashboardData)); side.add(menu("Complaints", () -> new ComplaintsFrame(residentId))); side.add(menu("Payments", () -> new PaymentsFrame(residentId))); side.add(menu("Refresh data", this::loadDashboardData));
        JButton logout = new JButton("Logout"); logout.setAlignmentX(Component.CENTER_ALIGNMENT); logout.setMaximumSize(new Dimension(155, 40)); logout.setBackground(new Color(95, 67, 48)); logout.setForeground(CREAM); logout.setFocusPainted(false);
        JPanel bottom = new JPanel(); bottom.setOpaque(false); bottom.setBorder(BorderFactory.createEmptyBorder(24, 0, 22, 0)); bottom.add(logout); side.add(Box.createVerticalGlue()); side.add(bottom); logout.addActionListener(e -> { new WelcomeFrame(); dispose(); });

        JPanel content = new JPanel(new BorderLayout(18, 18)); content.setOpaque(false); content.setBorder(BorderFactory.createEmptyBorder(26, 28, 24, 28)); root.add(content, BorderLayout.CENTER);
        JPanel header = new JPanel(new BorderLayout(10, 8)); header.setOpaque(false);
        JPanel titles = new JPanel(); titles.setOpaque(false); titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel hello = new JLabel("Welcome back, " + residentName); hello.setFont(new Font("Serif", Font.BOLD, 29)); hello.setForeground(COFFEE);
        JLabel subtitle = new JLabel("Your apartment at a glance"); subtitle.setForeground(new Color(126, 105, 86)); subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14)); titles.add(hello); titles.add(Box.createVerticalStrut(5)); titles.add(subtitle); header.add(titles, BorderLayout.WEST);
        JButton invite = LoginFrame.button("+  INVITE VISITOR"); invite.setPreferredSize(new Dimension(175, 42)); header.add(invite, BorderLayout.EAST); invite.addActionListener(e -> new InviteVisitorFrame(residentId, this));
        JPanel cards = new JPanel(new GridLayout(1, 3, 16, 0)); cards.setOpaque(false); cards.add(card("MY PARKING", parkingVal)); cards.add(card("VISITORS INVITED", visitorsVal)); cards.add(card("MAINTENANCE DUE", maintenanceVal));
        JPanel top = new JPanel(new BorderLayout(0, 20)); top.setOpaque(false); top.add(header, BorderLayout.NORTH); top.add(cards, BorderLayout.CENTER); content.add(top, BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new BorderLayout(0, 12)); tablePanel.setBackground(Color.WHITE); tablePanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE), BorderFactory.createEmptyBorder(18, 18, 14, 18)));
        JPanel tableHead = new JPanel(new BorderLayout()); tableHead.setOpaque(false); JLabel vt = new JLabel("Recent visitor passes"); vt.setFont(new Font("Serif", Font.BOLD, 22)); vt.setForeground(COFFEE); tableHead.add(vt, BorderLayout.WEST); loadState.setForeground(new Color(126, 105, 86)); tableHead.add(loadState, BorderLayout.EAST); tablePanel.add(tableHead, BorderLayout.NORTH);
        JTable table = new JTable(visitorModel); styleTable(table); tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        JLabel note = new JLabel("Vehicle visitor parking is assigned automatically when the pass is accepted at the gate."); note.setForeground(new Color(115, 95, 78)); note.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0)); tablePanel.add(note, BorderLayout.SOUTH); content.add(tablePanel, BorderLayout.CENTER);
        loadDashboardData(); setVisible(true);
    }
    private JButton menu(String text, Runnable action) { JButton b = new JButton(text); b.setMaximumSize(new Dimension(205, 44)); b.setAlignmentX(Component.CENTER_ALIGNMENT); b.setHorizontalAlignment(SwingConstants.LEFT); b.setBorder(BorderFactory.createEmptyBorder(9, 27, 9, 8)); b.setBackground(COFFEE); b.setForeground(CREAM); b.setFocusPainted(false); b.setOpaque(true); b.addActionListener(e -> action.run()); return b; }
    private JPanel card(String title, JLabel value) { JPanel p = new JPanel(new BorderLayout(0, 13)); p.setBackground(new Color(242, 231, 215)); p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(233, 218, 197)), BorderFactory.createEmptyBorder(17, 19, 17, 15))); JLabel t = new JLabel(title); t.setForeground(new Color(115, 82, 56)); t.setFont(new Font("SansSerif", Font.BOLD, 12)); value.setForeground(COFFEE); value.setFont(new Font("SansSerif", Font.BOLD, 26)); p.add(t, BorderLayout.NORTH); p.add(value, BorderLayout.CENTER); return p; }
    private void styleTable(JTable table) { table.setRowHeight(32); table.setFillsViewportHeight(true); table.setShowVerticalLines(false); table.setGridColor(new Color(242, 235, 226)); table.setSelectionBackground(new Color(242, 225, 201)); table.setSelectionForeground(COFFEE); table.getTableHeader().setBackground(new Color(239, 226, 209)); table.getTableHeader().setForeground(COFFEE); table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12)); }

    // Database work runs off the Swing event thread so the dashboard remains responsive.
    public void loadDashboardData() {
        loadState.setText("Loading…");
        new SwingWorker<Object[][], Void>() {
            protected Object[][] doInBackground() throws Exception {
                String slot = "Not assigned"; int count = 0; double due = 0;
                java.util.List<Object[]> rows = new java.util.ArrayList<>();
                try (Connection c = DBConnection.getConnection()) {
                    try (PreparedStatement p = c.prepareStatement("SELECT parking_slot FROM \"A-partment\".residents WHERE resident_id=?")) { p.setInt(1, residentId); try (ResultSet r = p.executeQuery()) { if (r.next() && r.getString(1) != null) slot = r.getString(1); } }
                    try (PreparedStatement p = c.prepareStatement("SELECT visitor_name,phone,COALESCE(parking_slot,'—') slot,status,valid_until FROM \"A-partment\".visitors WHERE resident_id=? ORDER BY visitor_id DESC")) { p.setInt(1, residentId); try (ResultSet r = p.executeQuery()) { while (r.next()) { rows.add(new Object[]{r.getString("visitor_name"), r.getString("phone"), r.getString("slot"), r.getString("status"), r.getTimestamp("valid_until")}); count++; } } }
                    try (PreparedStatement p = c.prepareStatement("SELECT COALESCE(SUM(amount),0) FROM \"A-partment\".payments WHERE resident_id=? AND status IN ('PENDING','OVERDUE')")) { p.setInt(1, residentId); try (ResultSet r = p.executeQuery()) { if (r.next()) due = r.getDouble(1); } }
                }
                Object[][] result = new Object[rows.size() + 1][]; result[0] = new Object[]{slot, count, due}; for (int i=0;i<rows.size();i++) result[i+1] = rows.get(i); return result;
            }
            protected void done() { try { Object[][] data = get(); Object[] summary = data[0]; parkingVal.setText(String.valueOf(summary[0])); visitorsVal.setText(String.valueOf(summary[1])); maintenanceVal.setText("\u20B9" + String.format("%.0f", (Double)summary[2])); visitorModel.setRowCount(0); for (int i=1;i<data.length;i++) visitorModel.addRow(data[i]); loadState.setText("Updated just now"); } catch (Exception e) { loadState.setText("Could not refresh"); LoginFrame.showDbError(e.getCause() instanceof Exception ? (Exception)e.getCause() : e); } }
        }.execute();
    }
}
