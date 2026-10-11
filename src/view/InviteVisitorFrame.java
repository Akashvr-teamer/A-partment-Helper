package view;

import database.DBConnection;
import util.CodeGenerator;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class InviteVisitorFrame extends JFrame {
    private final int residentId;
    private final ResidentDashboard dashboard;

    private final JTextField nameField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextField vehicleField = new JTextField();
    private final JCheckBox hasVehicleCheck = new JCheckBox("Visitor is arriving by vehicle");

    private final JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
    private final JSpinner startSpinner = new JSpinner(new SpinnerDateModel());
    private final JSpinner endSpinner = new JSpinner(new SpinnerDateModel());

    private static final Color COFFEE = new Color(55, 36, 27);
    private static final Color CREAM = new Color(250, 245, 235);

    public InviteVisitorFrame(int residentId, ResidentDashboard dashboard) {
        this.residentId = residentId;
        this.dashboard = dashboard;

        setTitle("Invite a visitor");
        setSize(500, 650);
        setLocationRelativeTo(dashboard);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(null);
        panel.setBackground(CREAM);
        setContentPane(panel);

        JLabel title = new JLabel("Create Visitor Pass");
        title.setBounds(30, 20, 420, 40);
        title.setFont(new Font("Serif", Font.BOLD, 27));
        title.setForeground(COFFEE);
        panel.add(title);

        addField(panel, "Visitor full name", nameField, 75);
        addField(panel, "Phone number", phoneField, 145);

        hasVehicleCheck.setBounds(35, 215, 300, 25);
        hasVehicleCheck.setBackground(CREAM);
        panel.add(hasVehicleCheck);

        addField(panel, "Vehicle registration", vehicleField, 260);
        vehicleField.setEnabled(false);
        hasVehicleCheck.addActionListener(e -> {
            boolean hasVehicle = hasVehicleCheck.isSelected();
            vehicleField.setEnabled(hasVehicle);
            if (!hasVehicle) {
                vehicleField.setText("");
            }
        });

        addLabel(panel, "Visit date", 35, 330);
        dateSpinner.setBounds(35, 355, 190, 35);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "dd MMM yyyy"));
        panel.add(dateSpinner);

        addLabel(panel, "Start time", 250, 330);
        startSpinner.setBounds(250, 355, 190, 35);
        startSpinner.setEditor(new JSpinner.DateEditor(startSpinner, "hh:mm a"));
        panel.add(startSpinner);

        addLabel(panel, "End time", 35, 410);
        endSpinner.setBounds(35, 435, 190, 35);
        endSpinner.setEditor(new JSpinner.DateEditor(endSpinner, "hh:mm a"));
        panel.add(endSpinner);

        JButton saveBtn = LoginFrame.button("GENERATE ACCESS PASS");
        saveBtn.setBounds(35, 515, 405, 48);
        panel.add(saveBtn);
        saveBtn.addActionListener(e -> saveVisitor());

        setVisible(true);
    }

    private void addField(JPanel p, String text, JTextField f, int y) {
        addLabel(p, text, 35, y);
        f.setBounds(35, y + 25, 405, 34);
        p.add(f);
    }

    private void addLabel(JPanel p, String s, int x, int y) {
        JLabel l = new JLabel(s);
        l.setBounds(x, y, 400, 20);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        l.setForeground(COFFEE);
        p.add(l);
    }

    // Validates inputs and creates a visitor pass record
    private void saveVisitor() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String vehicle = vehicleField.getText().trim();
        boolean hasVehicle = hasVehicleCheck.isSelected();

        if (name.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter visitor name and phone number.");
            return;
        }

        if (hasVehicle && vehicle.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the vehicle registration number.");
            return;
        }

        Date selectedDate = (Date) dateSpinner.getValue();
        Date startTime = (Date) startSpinner.getValue();
        Date endTime = (Date) endSpinner.getValue();

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(selectedDate);
        Calendar tempStart = Calendar.getInstance();
        tempStart.setTime(startTime);
        startCal.set(Calendar.HOUR_OF_DAY, tempStart.get(Calendar.HOUR_OF_DAY));
        startCal.set(Calendar.MINUTE, tempStart.get(Calendar.MINUTE));
        startCal.set(Calendar.SECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(selectedDate);
        Calendar tempEnd = Calendar.getInstance();
        tempEnd.setTime(endTime);
        endCal.set(Calendar.HOUR_OF_DAY, tempEnd.get(Calendar.HOUR_OF_DAY));
        endCal.set(Calendar.MINUTE, tempEnd.get(Calendar.MINUTE));
        endCal.set(Calendar.SECOND, 0);

        if (!endCal.after(startCal)) {
            JOptionPane.showMessageDialog(this, "End time must be after start time.");
            return;
        }

        // Generate 8-character code and SHA-256 hash
        String passCode = CodeGenerator.generateCode();
        String passHash = CodeGenerator.sha256(passCode);

        String sql = "INSERT INTO visitors(resident_id, visitor_name, phone, has_vehicle, vehicle_no, access_hash, valid_from, valid_until, status) " +
                     "VALUES(?, ?, ?, ?, ?, ?, ?, ?, 'PENDING')";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, residentId);
            ps.setString(2, name);
            ps.setString(3, phone);
            ps.setBoolean(4, hasVehicle);
            ps.setString(5, hasVehicle ? vehicle : null);
            ps.setString(6, passHash);
            ps.setTimestamp(7, new Timestamp(startCal.getTimeInMillis()));
            ps.setTimestamp(8, new Timestamp(endCal.getTimeInMillis()));
            ps.executeUpdate();

            // Refresh parent resident dashboard
            if (dashboard != null) {
                dashboard.loadDashboardData();
            }

            SimpleDateFormat fmt = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
            JOptionPane.showMessageDialog(this,
                    "PASS GENERATED SUCCESSFULLY!\n\n" +
                    "Visitor: " + name + "\n" +
                    "Pass Code: " + passCode + "\n" +
                    "Valid From: " + fmt.format(startCal.getTime()) + "\n" +
                    "Valid Until: " + fmt.format(endCal.getTime()) + "\n\n" +
                    "Share this pass code with your visitor.");

            dispose();
        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
        }
    }
}
