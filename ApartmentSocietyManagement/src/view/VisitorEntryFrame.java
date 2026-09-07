package view;

import javax.swing.*;
import java.awt.*;

public class VisitorEntryFrame extends JFrame {

    public VisitorEntryFrame() {

        setTitle("Visitor Entry");
        setSize(420, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        add(panel);

        JLabel label = new JLabel("VISITOR ENTRY");
        label.setFont(new Font("SansSerif", Font.BOLD, 24));
        panel.add(label);

        setVisible(true);
    }
}