package view;

import controller.ResidentController;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class InviteVisitorFrame extends JFrame {
    private final int residentId;
    private final ResidentDashboard dashboard;
    private final ResidentController residentController = new ResidentController();

    private final JTextField nameField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextField vehicleField = new JTextField();
    private final JCheckBox hasVehicleCheck = new JCheckBox("Visitor is arriving by vehicle");

    private final JSpinner startDateSpinner = new JSpinner(new SpinnerDateModel());
    private final JSpinner startTimeSpinner = new JSpinner(new SpinnerDateModel());
    private final JSpinner endDateSpinner = new JSpinner(new SpinnerDateModel());
    private final JSpinner endTimeSpinner = new JSpinner(new SpinnerDateModel());

    private static final Color COFFEE = new Color(55, 36, 27);
    private static final Color CREAM = new Color(250, 245, 235);

    public InviteVisitorFrame(int residentId, ResidentDashboard dashboard) {
        this.residentId = residentId;
        this.dashboard = dashboard;

        setTitle("Invite a visitor");
        setSize(485, 610);
        setLocationRelativeTo(dashboard);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel(null);
        panel.setBackground(CREAM);
        setContentPane(panel);

        JLabel title = new JLabel("Create Visitor Pass");
        title.setBounds(30, 20, 420, 40);
        title.setFont(new Font("Segoe UI", Font.BOLD, 27));
        title.setForeground(COFFEE);
        panel.add(title);

        addField(panel, "Visitor full name", nameField, 75);
        addField(panel, "Phone number", phoneField, 145);

        hasVehicleCheck.setBounds(35, 215, 300, 25);
        hasVehicleCheck.setBackground(CREAM);
        hasVehicleCheck.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(hasVehicleCheck);

        addField(panel, "Vehicle registration", vehicleField, 250);
        vehicleField.setEnabled(false);
        hasVehicleCheck.addActionListener(e -> {
            boolean hasVehicle = hasVehicleCheck.isSelected();
            vehicleField.setEnabled(hasVehicle);
            if (!hasVehicle) {
                vehicleField.setText("");
            }
        });

        // Valid From: Date and Time
        addLabel(panel, "Valid from (Date)", 35, 320);
        startDateSpinner.setBounds(35, 345, 190, 34);
        startDateSpinner.setEditor(new JSpinner.DateEditor(startDateSpinner, "dd MMM yyyy"));
        panel.add(startDateSpinner);

        addLabel(panel, "Start time", 250, 320);
        startTimeSpinner.setBounds(250, 345, 190, 34);
        startTimeSpinner.setEditor(new JSpinner.DateEditor(startTimeSpinner, "hh:mm a"));
        panel.add(startTimeSpinner);

        // Valid Until: Date and Time
        addLabel(panel, "Valid until (Date)", 35, 395);
        endDateSpinner.setBounds(35, 420, 190, 34);
        endDateSpinner.setEditor(new JSpinner.DateEditor(endDateSpinner, "dd MMM yyyy"));
        panel.add(endDateSpinner);

        addLabel(panel, "End time", 250, 395);
        endTimeSpinner.setBounds(250, 420, 190, 34);
        endTimeSpinner.setEditor(new JSpinner.DateEditor(endTimeSpinner, "hh:mm a"));
        panel.add(endTimeSpinner);

        JButton saveBtn = LoginFrame.button("GENERATE ACCESS PASS");
        saveBtn.setBounds(35, 495, 405, 48);
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
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
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

        Date startDate = (Date) startDateSpinner.getValue();
        Date startTime = (Date) startTimeSpinner.getValue();
        Date endDate = (Date) endDateSpinner.getValue();
        Date endTime = (Date) endTimeSpinner.getValue();

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);
        Calendar tempStart = Calendar.getInstance();
        tempStart.setTime(startTime);
        startCal.set(Calendar.HOUR_OF_DAY, tempStart.get(Calendar.HOUR_OF_DAY));
        startCal.set(Calendar.MINUTE, tempStart.get(Calendar.MINUTE));
        startCal.set(Calendar.SECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endDate);
        Calendar tempEnd = Calendar.getInstance();
        tempEnd.setTime(endTime);
        endCal.set(Calendar.HOUR_OF_DAY, tempEnd.get(Calendar.HOUR_OF_DAY));
        endCal.set(Calendar.MINUTE, tempEnd.get(Calendar.MINUTE));
        endCal.set(Calendar.SECOND, 0);

        if (!endCal.after(startCal)) {
            JOptionPane.showMessageDialog(this, "Valid until date and time must be after valid from date and time.");
            return;
        }

        try {
            Timestamp validFrom = new Timestamp(startCal.getTimeInMillis());
            Timestamp validUntil = new Timestamp(endCal.getTimeInMillis());

            String passCode = residentController.createVisitorPass(
                    residentId, name, phone, hasVehicle, vehicle, validFrom, validUntil
            );

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
