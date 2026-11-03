package view;

import controller.VisitorController;

import javax.swing.*;
import java.awt.*;

public class VisitorEntryFrame extends JFrame {
    private final JTextField codeField = new JTextField();

    private static final Color COFFEE = new Color(55, 36, 27);
    private static final Color CREAM = new Color(250, 245, 235);

    public VisitorEntryFrame() {
        setTitle("Visitor Gate Desk");
        setSize(460, 470);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel(null);
        panel.setBackground(COFFEE);
        setContentPane(panel);

        JLabel title = new JLabel("VISITOR GATE DESK", SwingConstants.CENTER);
        title.setBounds(25, 35, 400, 40);
        title.setForeground(CREAM);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        panel.add(title);

        JLabel sub = new JLabel("Enter the pass code to record entry or exit", SwingConstants.CENTER);
        sub.setBounds(25, 85, 400, 25);
        sub.setForeground(new Color(220, 195, 165));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(sub);

        JLabel label = new JLabel("Pass code (4-char) or Exit code (3-char)");
        label.setBounds(45, 145, 350, 22);
        label.setForeground(CREAM);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(label);

        codeField.setBounds(45, 173, 350, 45);
        codeField.setHorizontalAlignment(JTextField.CENTER);
        codeField.setFont(new Font("Monospaced", Font.BOLD, 20));
        panel.add(codeField);

        JButton entryBtn = LoginFrame.button("CONFIRM ENTRY");
        entryBtn.setBounds(45, 245, 350, 43);
        panel.add(entryBtn);

        JButton exitBtn = new JButton("MARK EXIT");
        exitBtn.setBounds(45, 305, 350, 40);
        exitBtn.setBackground(new Color(130, 76, 55));
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        exitBtn.setFocusPainted(false);
        panel.add(exitBtn);

        JButton backBtn = new JButton("Back to home");
        backBtn.setBounds(130, 365, 180, 30);
        backBtn.setForeground(CREAM);
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        panel.add(backBtn);

        entryBtn.addActionListener(e -> handleEntry());
        exitBtn.addActionListener(e -> handleExit());
        backBtn.addActionListener(e -> {
            new WelcomeFrame();
            dispose();
        });

        setVisible(true);
    }

    private final VisitorController visitorController = new VisitorController();

    // Handles visitor entry at gate via VisitorController
    private void handleEntry() {
        String code = codeField.getText().trim();
        try {
            VisitorController.EntryResult result = visitorController.processEntry(code);
            if (!result.success) {
                JOptionPane.showMessageDialog(this, result.message);
                return;
            }

            StringBuilder msg = new StringBuilder();
            msg.append("ENTRY CONFIRMED!\n\nWelcome, ").append(result.visitorName).append("!\n\n");
            if (result.parkingSlot != null) {
                if ("PARK OUTSIDE".equals(result.parkingSlot)) {
                    msg.append("Parking: VISITOR PARKING FULL\n(Advise visitor to park outside the premises)\n\n");
                } else {
                    msg.append("Assigned Parking Slot: ").append(result.parkingSlot).append("\n\n");
                }
            }
            msg.append("3-Character Exit Code: ").append(result.exitCode).append("\n\n");
            msg.append("Please give this 3-character exit code (").append(result.exitCode).append(") to the guard when leaving.");

            JOptionPane.showMessageDialog(this, msg.toString());
            codeField.setText("");
        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
        }
    }

    // Handles visitor exit at gate via VisitorController
    private void handleExit() {
        String code = codeField.getText().trim();
        try {
            VisitorController.ExitResult result = visitorController.processExit(code);
            if (!result.success) {
                JOptionPane.showMessageDialog(this, result.message);
                return;
            }

            StringBuilder msg = new StringBuilder();
            msg.append("EXIT CONFIRMED!\n\nGoodbye, ").append(result.visitorName).append("!");
            if (result.freedParkingSlot != null && !result.freedParkingSlot.isBlank()) {
                msg.append("\nVisitor parking slot ").append(result.freedParkingSlot).append(" is now AVAILABLE.");
            }
            JOptionPane.showMessageDialog(this, msg.toString());
            codeField.setText("");
        } catch (Exception ex) {
            LoginFrame.showDbError(ex);
        }
    }
}
