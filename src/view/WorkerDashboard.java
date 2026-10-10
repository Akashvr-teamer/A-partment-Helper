package view;

import database.DBConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class WorkerDashboard extends JFrame {
    private final int workerId;
    private final String workerName;
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Resident", "Apartment", "Title", "Category", "Status", "Assigned worker"}, 0) { public boolean isCellEditable(int r, int c) { return false; } };
    private final DefaultTableModel parkingModel = new DefaultTableModel(new String[]{"Slot", "Type", "Status", "Resident / Visitor"}, 0) { public boolean isCellEditable(int r, int c) { return false; } };
    private final JLabel state = new JLabel("Loading dashboard…");
    private static final Color COFFEE = new Color(55,36,27), CREAM = new Color(250,245,235), LINE = new Color(229,216,199);

    public WorkerDashboard(int id, String name) {
        workerId=id; workerName=name; setTitle("A-partment Helper | Worker Desk"); setSize(1180,760); setMinimumSize(new Dimension(950,650)); setLocationRelativeTo(null); setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout(18,18)); root.setBackground(CREAM); root.setBorder(BorderFactory.createEmptyBorder(24,28,22,28)); setContentPane(root);
        JPanel head = new JPanel(new BorderLayout(10,8)); head.setOpaque(false); JPanel heading = new JPanel(); heading.setOpaque(false); heading.setLayout(new BoxLayout(heading,BoxLayout.Y_AXIS)); JLabel title = new JLabel("Worker desk"); title.setFont(new Font("Serif",Font.BOLD,30)); title.setForeground(COFFEE); JLabel sub = new JLabel("Welcome, " + workerName + "  ·  Complaints and parking overview"); sub.setForeground(new Color(126,105,86)); heading.add(title); heading.add(Box.createVerticalStrut(5)); heading.add(sub); head.add(heading,BorderLayout.WEST);
        JPanel headRight = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,4)); headRight.setOpaque(false); JButton refresh = LoginFrame.button("REFRESH"); JButton logout = new JButton("Logout"); logout.setFocusPainted(false); headRight.add(refresh); headRight.add(logout); head.add(headRight,BorderLayout.EAST); root.add(head,BorderLayout.NORTH);
        JTable complaints = new JTable(model); styleTable(complaints); complaints.setAutoCreateRowSorter(true); JPanel complaintsPanel = panel("Maintenance complaints",new JScrollPane(complaints));
        JTable parking = new JTable(parkingModel); styleTable(parking); parking.setAutoCreateRowSorter(true); JPanel parkingPanel = panel("Parking occupancy",new JScrollPane(parking));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,complaintsPanel,parkingPanel); split.setResizeWeight(0.68); split.setDividerSize(8); split.setBorder(null); split.setOpaque(false); root.add(split,BorderLayout.CENTER);
        JPanel actions = new JPanel(new BorderLayout()); actions.setOpaque(false); state.setForeground(new Color(126,105,86)); actions.add(state,BorderLayout.WEST); JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); buttons.setOpaque(false); JButton claim=LoginFrame.button("CLAIM SELECTED"), progress=LoginFrame.button("MARK IN PROGRESS"), complete=LoginFrame.button("MARK COMPLETED"); buttons.add(claim); buttons.add(progress); buttons.add(complete); actions.add(buttons,BorderLayout.EAST); root.add(actions,BorderLayout.SOUTH);
        refresh.addActionListener(e->load()); logout.addActionListener(e->{new WelcomeFrame();dispose();}); claim.addActionListener(e->update(complaints,"ASSIGNED")); progress.addActionListener(e->update(complaints,"IN_PROGRESS")); complete.addActionListener(e->update(complaints,"COMPLETED")); load(); setVisible(true);
    }
    private JPanel panel(String title, JComponent content) { JPanel p=new JPanel(new BorderLayout(0,10)); p.setBackground(Color.WHITE); p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),BorderFactory.createEmptyBorder(14,16,14,16))); JLabel label=new JLabel(title); label.setFont(new Font("Serif",Font.BOLD,20)); label.setForeground(COFFEE); p.add(label,BorderLayout.NORTH); p.add(content,BorderLayout.CENTER); return p; }
    private void styleTable(JTable t) { t.setRowHeight(29); t.setFillsViewportHeight(true); t.setShowVerticalLines(false); t.setGridColor(new Color(242,235,226)); t.setSelectionBackground(new Color(242,225,201)); t.setSelectionForeground(COFFEE); t.getTableHeader().setBackground(new Color(239,226,209)); t.getTableHeader().setForeground(COFFEE); t.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,12)); }
    private void load() {
        state.setText("Refreshing complaints and parking…");
        new SwingWorker<Object[][],Void>() {
            protected Object[][] doInBackground() throws Exception {
                java.util.List<Object[]> complaints=new java.util.ArrayList<>(), slots=new java.util.ArrayList<>();
                try(Connection c=DBConnection.getConnection()) {
                    String sql="SELECT c.complaint_id,r.full_name,r.apartment_no,c.title,c.category,c.status,COALESCE(w.full_name,'Unassigned') worker FROM \"A-partment\".complaints c JOIN \"A-partment\".residents r ON r.resident_id=c.resident_id LEFT JOIN \"A-partment\".workers w ON w.worker_id=c.worker_id ORDER BY CASE c.status WHEN 'PENDING' THEN 0 WHEN 'ASSIGNED' THEN 1 WHEN 'IN_PROGRESS' THEN 2 ELSE 3 END,c.created_at";
                    try(PreparedStatement s=c.prepareStatement(sql);ResultSet r=s.executeQuery()){while(r.next())complaints.add(new Object[]{r.getInt("complaint_id"),r.getString("full_name"),r.getString("apartment_no"),r.getString("title"),r.getString("category"),r.getString("status"),r.getString("worker")});}
                    String ps="SELECT p.slot_number,p.parking_type,p.status,COALESCE(r.full_name,v.visitor_name,'—') occupant FROM \"A-partment\".parking p LEFT JOIN \"A-partment\".residents r ON r.resident_id=p.resident_id LEFT JOIN \"A-partment\".visitors v ON v.visitor_id=p.visitor_id ORDER BY p.parking_type,p.slot_number";
                    try(PreparedStatement s=c.prepareStatement(ps);ResultSet r=s.executeQuery()){while(r.next())slots.add(new Object[]{r.getString("slot_number"),r.getString("parking_type"),r.getString("status"),r.getString("occupant")});}
                }
                return new Object[][]{complaints.toArray(new Object[0][]),slots.toArray(new Object[0][])};
            }
            protected void done(){try{Object[][][] result=(Object[][][])get();model.setRowCount(0);for(Object[] r:result[0])model.addRow(r);parkingModel.setRowCount(0);for(Object[] r:result[1])parkingModel.addRow(r);state.setText("Updated just now · " + result[0].length + " complaints · " + result[1].length + " parking slots");}catch(Exception ex){state.setText("Refresh failed");LoginFrame.showDbError(ex.getCause() instanceof Exception ? (Exception)ex.getCause() : ex);}}
        }.execute();
    }
    private void update(JTable table,String status){int row=table.getSelectedRow();if(row<0){JOptionPane.showMessageDialog(this,"Select a complaint first.");return;}int modelRow=table.convertRowIndexToModel(row);int complaintId=(int)model.getValueAt(modelRow,0);String current=String.valueOf(model.getValueAt(modelRow,5));if("COMPLETED".equals(current)){JOptionPane.showMessageDialog(this,"This complaint is already completed.");return;}String sql;if(status.equals("ASSIGNED")){if(!current.equals("PENDING")&&!current.equals("ASSIGNED")){JOptionPane.showMessageDialog(this,"Only pending or already assigned complaints can be claimed.");return;}sql="UPDATE \"A-partment\".complaints SET worker_id=?,status='ASSIGNED' WHERE complaint_id=? AND (worker_id IS NULL OR worker_id=?) AND status IN ('PENDING','ASSIGNED')";}else{if(!current.equals("ASSIGNED")&&!current.equals("IN_PROGRESS")){JOptionPane.showMessageDialog(this,"Claim this complaint before updating it.");return;}sql=status.equals("IN_PROGRESS")?"UPDATE \"A-partment\".complaints SET status='IN_PROGRESS' WHERE complaint_id=? AND worker_id=?":"UPDATE \"A-partment\".complaints SET status='COMPLETED',completed_at=CURRENT_TIMESTAMP WHERE complaint_id=? AND worker_id=?";}try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql)){if(status.equals("ASSIGNED")){s.setInt(1,workerId);s.setInt(2,complaintId);s.setInt(3,workerId);}else{s.setInt(1,complaintId);s.setInt(2,workerId);}int n=s.executeUpdate();JOptionPane.showMessageDialog(this,n==1?"Complaint updated to "+status+".":"This complaint is assigned to another worker or has changed. Refresh the list.");load();}catch(Exception ex){LoginFrame.showDbError(ex);}}
}
