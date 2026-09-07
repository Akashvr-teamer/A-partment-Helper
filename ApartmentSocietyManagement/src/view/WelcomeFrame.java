package view;

import javax.swing.*;
import java.awt.*;

public class WelcomeFrame extends JFrame {

    public WelcomeFrame() {

        setTitle("Smart Apartment Society");
        setSize(420, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(8, 23, 53));
        add(panel);

        // Title
        JLabel title = new JLabel("Smart Apartment Society");
        title.setBounds(20, 40, 360, 40);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(title);

        // Subtitle
        JLabel subtitle = new JLabel("Secure • Smart • Connected");
        subtitle.setBounds(70, 80, 260, 20);
        subtitle.setForeground(new Color(180, 200, 255));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(subtitle);

        // LOGIN BUTTON
        JButton loginBtn = createButton(
                "Resident / Worker Login",
                new Color(37, 99, 235));

        loginBtn.setBounds(50, 180, 300, 65);
        panel.add(loginBtn);

        // VISITOR BUTTON
        JButton visitorBtn = createButton(
                "Visitor Entry",
                new Color(16, 185, 129));

        visitorBtn.setBounds(50, 280, 300, 65);
        panel.add(visitorBtn);

        // Footer
        JLabel footer = new JLabel("Apartment Society Management System");
        footer.setBounds(35, 530, 340, 20);
        footer.setForeground(new Color(140, 140, 140));
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(footer);

        // Navigation (temporary)
        loginBtn.addActionListener(e -> {
            new LoginFrame();
            dispose();
        });

        visitorBtn.addActionListener(e -> {
            new VisitorEntryFrame();
            dispose();
        });

        setVisible(true);
    }

    private JButton createButton(String text, Color color) {

        JButton btn = new JButton(text);

        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFont(new Font("SansSerif", Font.BOLD, 18));

        return btn;
    }
}