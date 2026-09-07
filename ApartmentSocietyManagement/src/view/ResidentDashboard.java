package view;

import javax.swing.*;
import java.awt.*;

public class ResidentDashboard extends JFrame {

    public ResidentDashboard() {

        setTitle("Resident Dashboard");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel main = new JPanel(null);
        main.setBackground(new Color(243,244,246));
        add(main);

        // ---------- SIDEBAR ----------
        JPanel side = new JPanel(null);
        side.setBounds(0,0,180,600);
        side.setBackground(new Color(17,24,39));
        main.add(side);

        JLabel logo = new JLabel("SASMS");
        logo.setBounds(50,25,100,30);
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("SansSerif",Font.BOLD,24));
        side.add(logo);

        side.add(menuButton("Dashboard",70,true));
        side.add(menuButton("Visitors",120,false));
        side.add(menuButton("Complaints",170,false));
        side.add(menuButton("Parking",220,false));
        side.add(menuButton("Payments",270,false));

        JButton logout = new JButton("Logout");
        logout.setBounds(30,510,120,35);
        side.add(logout);

        // ---------- HEADER ----------
        JLabel welcome = new JLabel("Welcome, Akash 👋");
        welcome.setBounds(210,20,300,30);
        welcome.setFont(new Font("SansSerif",Font.BOLD,24));
        main.add(welcome);

        // ---------- CARDS ----------
        main.add(createCard("My Parking","A-01",
                new Color(219,234,254),210,70));

        main.add(createCard("Visitors","03",
                new Color(209,250,229),430,70));

        main.add(createCard("Maintenance","₹2500",
                new Color(252,231,243),650,70));

        // ---------- VISITOR PANEL ----------
        JPanel visitors = new JPanel(null);
        visitors.setBounds(210,200,620,320);
        visitors.setBackground(Color.WHITE);
        visitors.setBorder(BorderFactory.createLineBorder(
                new Color(220,220,220)));

        JLabel t = new JLabel("Today's Visitors");
        t.setBounds(20,15,200,25);
        t.setFont(new Font("SansSerif",Font.BOLD,18));
        visitors.add(t);

        String[] columns = {
                "Visitor","Parking","Status"
        };

        String[][] data = {
                {"Sarah","V-08","Entered"},
                {"Arjun","V-02","Pending"},
                {"Meera","Walk-in","Exited"}
        };

        JTable table = new JTable(data,columns);

        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(20,55,580,220);
        visitors.add(sp);

        JButton invite = new JButton("Invite Visitor");
        invite.addActionListener(e -> new InviteVisitorFrame());
        invite.setBounds(420,285,180,25);
        invite.setBackground(new Color(37,99,235));
        invite.setForeground(Color.WHITE);
        visitors.add(invite);

        main.add(visitors);

        setVisible(true);
    }

    private JButton menuButton(String text,int y,boolean active){

        JButton b = new JButton(text);
        b.setBounds(20,y,140,35);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);

        if(active){
            b.setBackground(new Color(37,99,235));
            b.setForeground(Color.WHITE);
        }else{
            b.setBackground(new Color(17,24,39));
            b.setForeground(new Color(210,210,210));
        }

        return b;
    }

    private JPanel createCard(String title,
                              String value,
                              Color color,
                              int x,int y){

        JPanel card = new JPanel(null);
        card.setBounds(x,y,180,100);
        card.setBackground(color);

        JLabel t = new JLabel(title);
        t.setBounds(15,10,150,20);
        t.setFont(new Font("SansSerif",Font.PLAIN,15));

        JLabel v = new JLabel(value);
        v.setBounds(15,40,150,35);
        v.setFont(new Font("SansSerif",Font.BOLD,26));

        card.add(t);
        card.add(v);

        return card;
    }
}