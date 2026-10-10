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
    private final int residentId; private final ResidentDashboard dashboard;
    private final JTextField name = new JTextField(), phone = new JTextField(), vehicle = new JTextField();
    private final JCheckBox hasVehicle = new JCheckBox("Visitor is arriving by vehicle");
    private final JSpinner date = new JSpinner(new SpinnerDateModel()), start = new JSpinner(new SpinnerDateModel()), end = new JSpinner(new SpinnerDateModel());
    private static final Color COFFEE=new Color(55,36,27), CREAM=new Color(250,245,235), AMBER=new Color(190,124,58);
    public InviteVisitorFrame(int residentId, ResidentDashboard dashboard) {
        this.residentId=residentId; this.dashboard=dashboard; setTitle("Invite a visitor"); setSize(500,650); setLocationRelativeTo(dashboard); setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel p=new JPanel(null); p.setBackground(CREAM); setContentPane(p); JLabel title=new JLabel("Create Visitor Pass"); title.setBounds(30,20,420,40); title.setFont(new Font("Serif",Font.BOLD,27)); title.setForeground(COFFEE); p.add(title);
        field(p,"Visitor full name",name,75); field(p,"Phone number",phone,145); hasVehicle.setBounds(35,215,300,25); hasVehicle.setBackground(CREAM); p.add(hasVehicle); field(p,"Vehicle registration",vehicle,260); vehicle.setEnabled(false); hasVehicle.addActionListener(e->{vehicle.setEnabled(hasVehicle.isSelected()); if(!hasVehicle.isSelected())vehicle.setText("");});
        label(p,"Visit date",35,330); date.setBounds(35,355,190,35); date.setEditor(new JSpinner.DateEditor(date,"dd MMM yyyy")); p.add(date); label(p,"Start time",250,330); start.setBounds(250,355,190,35); start.setEditor(new JSpinner.DateEditor(start,"hh:mm a")); p.add(start); label(p,"End time",35,410); end.setBounds(35,435,190,35); end.setEditor(new JSpinner.DateEditor(end,"hh:mm a")); p.add(end);
        JButton save=LoginFrame.button("GENERATE ACCESS PASS"); save.setBounds(35,515,405,48); p.add(save); save.addActionListener(e->saveVisitor());
        setVisible(true);
    }
    private void field(JPanel p,String text,JTextField f,int y){label(p,text,35,y);f.setBounds(35,y+25,405,34);p.add(f);} private void label(JPanel p,String s,int x,int y){JLabel l=new JLabel(s);l.setBounds(x,y,400,20);l.setFont(new Font("SansSerif",Font.BOLD,13));l.setForeground(COFFEE);p.add(l);}
    private void saveVisitor(){
        String n=name.getText().trim(), ph=phone.getText().trim(), v=vehicle.getText().trim(); boolean car=hasVehicle.isSelected();
        if(n.isEmpty()||ph.isEmpty()){JOptionPane.showMessageDialog(this,"Enter visitor name and phone number.");return;}
        if(!ph.matches("[+0-9() -]{7,15}")){JOptionPane.showMessageDialog(this,"Enter a valid phone number.");return;} if(car&&v.isEmpty()){JOptionPane.showMessageDialog(this,"Enter the vehicle registration.");return;}
        Date d=(Date)date.getValue(), st=(Date)start.getValue(), en=(Date)end.getValue(); Calendar a=Calendar.getInstance(), b=Calendar.getInstance(); a.setTime(d); b.setTime(d); Calendar ts=Calendar.getInstance(),te=Calendar.getInstance();ts.setTime(st);te.setTime(en); a.set(Calendar.HOUR_OF_DAY,ts.get(Calendar.HOUR_OF_DAY));a.set(Calendar.MINUTE,ts.get(Calendar.MINUTE));a.set(Calendar.SECOND,0);a.set(Calendar.MILLISECOND,0); b.set(Calendar.HOUR_OF_DAY,te.get(Calendar.HOUR_OF_DAY));b.set(Calendar.MINUTE,te.get(Calendar.MINUTE));b.set(Calendar.SECOND,0);b.set(Calendar.MILLISECOND,0);
        if(!b.after(a)){JOptionPane.showMessageDialog(this,"End time must be later than start time. For overnight visits, use a later date.");return;} if(b.getTime().before(new Date())){JOptionPane.showMessageDialog(this,"The visit end time is already in the past.");return;}
        String code=CodeGenerator.generateCode(), hash=CodeGenerator.sha256(code); String sql="INSERT INTO \"A-partment\".visitors(resident_id,visitor_name,phone,has_vehicle,vehicle_no,access_hash,valid_from,valid_until,status) VALUES(?,?,?,?,?,?,?,?,'PENDING')";
        try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(sql)){ps.setInt(1,residentId);ps.setString(2,n);ps.setString(3,ph);ps.setBoolean(4,car);ps.setString(5,car?v:null);ps.setString(6,hash);ps.setTimestamp(7,new Timestamp(a.getTimeInMillis()));ps.setTimestamp(8,new Timestamp(b.getTimeInMillis()));ps.executeUpdate(); if(dashboard!=null)dashboard.loadDashboardData(); JOptionPane.showMessageDialog(this,"ACCESS PASS CREATED\n\nVisitor: "+n+"\nPass code: "+code+"\nValid from: "+new SimpleDateFormat("dd MMM yyyy, hh:mm a").format(a.getTime())+"\nValid until: "+new SimpleDateFormat("dd MMM yyyy, hh:mm a").format(b.getTime())+"\n\nSave this code and share it only with the visitor.");dispose();}catch(Exception ex){LoginFrame.showDbError(ex);}
    }
}
