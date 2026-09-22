package view;

import database.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JRadioButton residentBtn;
    private JRadioButton workerBtn;

    public LoginFrame() {

        setTitle("Login");
        setSize(420, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(8,23,53));
        add(panel);

        JLabel title = new JLabel("LOGIN");
        title.setBounds(120,40,180,40);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif",Font.BOLD,28));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(title);

        JLabel email = new JLabel("Email");
        email.setBounds(50,120,100,20);
        email.setForeground(Color.WHITE);
        panel.add(email);

        emailField = new JTextField();
        emailField.setBounds(50,145,300,40);
        panel.add(emailField);

        JLabel pass = new JLabel("Password");
        pass.setBounds(50,205,100,20);
        pass.setForeground(Color.WHITE);
        panel.add(pass);

        passwordField = new JPasswordField();
        passwordField.setBounds(50,230,300,40);
        panel.add(passwordField);

        residentBtn = new JRadioButton("Resident");
        residentBtn.setBounds(60,300,100,30);
        residentBtn.setBackground(new Color(8,23,53));
        residentBtn.setForeground(Color.WHITE);

        workerBtn = new JRadioButton("Worker");
        workerBtn.setBounds(220,300,100,30);
        workerBtn.setBackground(new Color(8,23,53));
        workerBtn.setForeground(Color.WHITE);

        ButtonGroup group = new ButtonGroup();
        group.add(residentBtn);
        group.add(workerBtn);
        residentBtn.setSelected(true);

        panel.add(residentBtn);
        panel.add(workerBtn);

        JButton login = new JButton("LOGIN");
        login.setBounds(100,380,200,45);
        login.setBackground(new Color(37,99,235));
        login.setForeground(Color.WHITE);
        login.setFocusPainted(false);
        login.setFont(new Font("SansSerif",Font.BOLD,16));
        panel.add(login);

        JButton back = new JButton("Back");
        back.setBounds(150,450,100,30);
        panel.add(back);

        login.addActionListener(e -> authenticate());

        back.addActionListener(e -> {
            new WelcomeFrame();
            dispose();
        });

        setVisible(true);
    }

    private void authenticate(){

        String email = emailField.getText();
        String password = String.valueOf(passwordField.getPassword());

        String table = residentBtn.isSelected() ? "residents" : "workers";

        try{

            Connection con = DBConnection.getConnection();

            String sql;

            if(table.equals("residents")){
                sql = "SELECT * FROM residents WHERE email=? AND password=?";
            }else{
                sql = "SELECT * FROM workers WHERE email=? AND password=?";
            }

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1,email);
            ps.setString(2,password);

            ResultSet rs = ps.executeQuery();

            if(rs.next()){

                JOptionPane.showMessageDialog(this,"Login Successful!");

                if(table.equals("residents")){
                    int id = rs.getInt("resident_id");
                    String name = rs.getString("full_name");
                    new ResidentDashboard(id, name);
                }else{
                    new WorkerDashboard();
                }

                dispose();

            }else{
                JOptionPane.showMessageDialog(this,
                        "Invalid Credentials");
            }

            con.close();

        }catch(Exception e){
            e.printStackTrace();
        }

    }
}