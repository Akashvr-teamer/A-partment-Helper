package view;

import database.DBConnection;
import util.CodeGenerator;

import javax.swing.*;
import java.awt.*;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class InviteVisitorFrame extends JFrame {

    private JTextField nameField, phoneField, vehicleField;
    private JCheckBox vehicleCheck;

    private JSpinner dateSpinner;
    private JSpinner startSpinner;
    private JSpinner endSpinner;

    public InviteVisitorFrame() {

        setTitle("Invite Visitor");
        setSize(470,650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(245,247,250));
        add(panel);

        JLabel title = new JLabel("Invite Visitor");
        title.setFont(new Font("SansSerif",Font.BOLD,26));
        title.setBounds(110,25,250,35);
        panel.add(title);

        // Name
        panel.add(label("Visitor Name",35,85));
        nameField = field(35,110);
        panel.add(nameField);

        // Phone
        panel.add(label("Phone Number",35,165));
        phoneField = field(35,190);
        panel.add(phoneField);

        // Vehicle checkbox
        vehicleCheck = new JCheckBox("Arriving by Vehicle");
        vehicleCheck.setBounds(35,250,180,25);
        vehicleCheck.setBackground(panel.getBackground());
        panel.add(vehicleCheck);

        panel.add(label("Vehicle Number",35,285));
        vehicleField = field(35,310);
        vehicleField.setEnabled(false);
        panel.add(vehicleField);

        vehicleCheck.addActionListener(e -> {
            vehicleField.setEnabled(vehicleCheck.isSelected());
            if(!vehicleCheck.isSelected())
                vehicleField.setText("");
        });

        // Date
        panel.add(label("Visit Date",35,365));

        dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setBounds(35,390,180,35);
        JSpinner.DateEditor dateEditor =
                new JSpinner.DateEditor(dateSpinner,"dd/MM/yyyy");
        dateSpinner.setEditor(dateEditor);
        panel.add(dateSpinner);

        // Start Time
        panel.add(label("Start Time",240,365));

        startSpinner = new JSpinner(new SpinnerDateModel());
        startSpinner.setBounds(240,390,180,35);
        JSpinner.DateEditor startEditor =
                new JSpinner.DateEditor(startSpinner,"hh:mm a");
        startSpinner.setEditor(startEditor);
        panel.add(startSpinner);

        // End Time
        panel.add(label("End Time",35,445));

        endSpinner = new JSpinner(new SpinnerDateModel());
        endSpinner.setBounds(35,470,180,35);
        JSpinner.DateEditor endEditor =
                new JSpinner.DateEditor(endSpinner,"hh:mm a");
        endSpinner.setEditor(endEditor);
        panel.add(endSpinner);

        JButton generate = new JButton("Generate Access Pass");
        generate.setBounds(70,550,300,45);
        generate.setBackground(new Color(37,99,235));
        generate.setForeground(Color.WHITE);
        generate.setFocusPainted(false);
        generate.setFont(new Font("SansSerif",Font.BOLD,16));
        panel.add(generate);

        generate.addActionListener(e -> saveVisitor());

        setVisible(true);
    }

    // ---------- UI Helpers ----------

    private JLabel label(String text,int x,int y){
        JLabel l = new JLabel(text);
        l.setBounds(x,y,140,20);
        l.setFont(new Font("SansSerif",Font.BOLD,14));
        return l;
    }

    private JTextField field(int x,int y){
        JTextField t = new JTextField();
        t.setBounds(x,y,385,38);
        return t;
    }

    // ---------- SAVE ----------

    private void saveVisitor(){

        String name = nameField.getText();
        String phone = phoneField.getText();
        String vehicle = vehicleField.getText();

        boolean hasVehicle = vehicleCheck.isSelected();

        if(name.isEmpty() || phone.isEmpty()){
            JOptionPane.showMessageDialog(this,
                    "Fill all required fields");
            return;
        }

        if(hasVehicle && vehicle.isEmpty()){
            JOptionPane.showMessageDialog(this,
                    "Enter vehicle number");
            return;
        }

        Date visitDate = (Date) dateSpinner.getValue();
        Date start = (Date) startSpinner.getValue();
        Date end = (Date) endSpinner.getValue();

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(visitDate);

        Calendar time1 = Calendar.getInstance();
        time1.setTime(start);

        startCal.set(Calendar.HOUR_OF_DAY,time1.get(Calendar.HOUR_OF_DAY));
        startCal.set(Calendar.MINUTE,time1.get(Calendar.MINUTE));

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(visitDate);

        Calendar time2 = Calendar.getInstance();
        time2.setTime(end);

        endCal.set(Calendar.HOUR_OF_DAY,time2.get(Calendar.HOUR_OF_DAY));
        endCal.set(Calendar.MINUTE,time2.get(Calendar.MINUTE));

        if(endCal.before(startCal)){
            JOptionPane.showMessageDialog(this,
                    "End time must be after start time");
            return;
        }

        String code = CodeGenerator.generateCode();
        String hash = sha256(code);

        try{

            Connection con = DBConnection.getConnection();

            String sql =
                    "INSERT INTO visitors(resident_id,visitor_name,phone,has_vehicle,vehicle_no,access_hash,valid_from,valid_until) VALUES(?,?,?,?,?,?,?,?)";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1,1); // temporary resident id
            ps.setString(2,name);
            ps.setString(3,phone);
            ps.setBoolean(4,hasVehicle);
            ps.setString(5,hasVehicle ? vehicle : null);
            ps.setString(6,hash);

            SimpleDateFormat f =
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            ps.setString(7,f.format(startCal.getTime()));
            ps.setString(8,f.format(endCal.getTime()));

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "ACCESS PASS GENERATED\n\n"+
                            "Visitor : "+name+
                            "\nCode : "+code+
                            "\nValid : "+
                            new SimpleDateFormat("dd MMM hh:mm a")
                                    .format(startCal.getTime())
                            +" - "+
                            new SimpleDateFormat("hh:mm a")
                                    .format(endCal.getTime()));

            dispose();

            con.close();

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    // ---------- SHA256 ----------

    private String sha256(String input){

        try{

            MessageDigest md =
                    MessageDigest.getInstance("SHA-256");

            byte[] bytes = md.digest(input.getBytes());

            StringBuilder sb = new StringBuilder();

            for(byte b : bytes)
                sb.append(String.format("%02x",b));

            return sb.toString();

        }catch(Exception e){
            return "";
        }
    }
}