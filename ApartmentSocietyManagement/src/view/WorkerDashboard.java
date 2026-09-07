package view;

import javax.swing.*;
import java.awt.*;

public class WorkerDashboard extends JFrame{

    public WorkerDashboard(){

        setTitle("Worker Dashboard");
        setSize(700,500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel label = new JLabel("WELCOME WORKER");
        label.setFont(new Font("SansSerif",Font.BOLD,28));
        label.setHorizontalAlignment(SwingConstants.CENTER);

        add(label);

        setVisible(true);
    }
}