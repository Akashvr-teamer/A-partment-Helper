package view;

import javax.swing.*;
import java.awt.*;

public class WelcomeFrame extends JFrame {
    private static final Color COFFEE = new Color(55, 36, 27), AMBER = new Color(190, 124, 58),
            CREAM = new Color(250, 245, 235);

    public WelcomeFrame() {
        setTitle("A-partment Helper");
        setSize(480, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        JPanel p = new JPanel(null);
        p.setBackground(COFFEE);
        setContentPane(p);
        JLabel mark = new JLabel("A", SwingConstants.CENTER);
        mark.setBounds(180, 55, 120, 95);
        mark.setOpaque(true);
        mark.setBackground(AMBER);
        mark.setForeground(COFFEE);
        mark.setFont(new Font("Serif", Font.BOLD, 64));
        p.add(mark);
        JLabel title = new JLabel("A-partment Helper", SwingConstants.CENTER);
        title.setBounds(30, 175, 420, 42);
        title.setForeground(CREAM);
        title.setFont(new Font("Serif", Font.BOLD, 30));
        p.add(title);
        JLabel sub = new JLabel("A smarter, warmer way to manage your society", SwingConstants.CENTER);
        sub.setBounds(25, 220, 430, 26);
        sub.setForeground(new Color(220, 195, 165));
        p.add(sub);
        JButton login = LoginFrame.button("RESIDENT / WORKER LOGIN");
        login.setBounds(65, 300, 350, 55);
        p.add(login);
        login.addActionListener(e -> {
            new LoginFrame();
            dispose();
        });
        JButton visitor = new JButton("VISITOR GATE ENTRY");
        visitor.setBounds(65, 375, 350, 50);
        visitor.setBackground(new Color(95, 67, 48));
        visitor.setForeground(CREAM);
        visitor.setFont(new Font("SansSerif", Font.BOLD, 14));
        visitor.setFocusPainted(false);
        p.add(visitor);
        visitor.addActionListener(e -> {
            new VisitorEntryFrame();
            dispose();
        });
        JLabel foot = new JLabel("SECURE  •  SIMPLE  •  CONNECTED", SwingConstants.CENTER);
        foot.setBounds(40, 500, 400, 25);
        foot.setForeground(new Color(190, 160, 125));
        p.add(foot);
        setVisible(true);
    }
}
