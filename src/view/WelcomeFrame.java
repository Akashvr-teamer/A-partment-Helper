package view;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import javax.imageio.ImageIO;

public class WelcomeFrame extends JFrame {
    private static final Color COFFEE = new Color(55, 36, 27);
    private static final Color AMBER = new Color(190, 124, 58);
    private static final Color CREAM = new Color(250, 245, 235);

    public WelcomeFrame() {
        setTitle("A-partment Helper");
        setSize(480, 610);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        BufferedImage bgImage = loadBackgroundImage();
        BackgroundPanel panel = new BackgroundPanel(bgImage);
        setContentPane(panel);

        // Rounded button for Resident / Worker Login
        RoundedButton login = new RoundedButton("RESIDENT / WORKER LOGIN", AMBER, Color.WHITE, 26);
        login.setBounds(65, 320, 350, 52);
        login.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(login);
        login.addActionListener(e -> {
            new LoginFrame();
            dispose();
        });

        // Rounded button for Visitor Gate Entry
        RoundedButton visitor = new RoundedButton("VISITOR GATE ENTRY", new Color(95, 67, 48), CREAM, 26);
        visitor.setBounds(65, 390, 350, 50);
        visitor.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(visitor);
        visitor.addActionListener(e -> {
            new VisitorEntryFrame();
            dispose();
        });

        // Subtle bottom tagline
        JLabel foot = new JLabel("SECURE  •  SIMPLE  •  CONNECTED", SwingConstants.CENTER);
        foot.setBounds(40, 520, 400, 25);
        foot.setForeground(new Color(210, 185, 155));
        foot.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        panel.add(foot);

        setVisible(true);
    }

    private static BufferedImage loadBackgroundImage() {
        // 1. Try classloader / classpath resources
        try {
            URL url = WelcomeFrame.class.getResource("/resources/background.jpg");
            if (url == null) {
                url = WelcomeFrame.class.getResource("background.jpg");
            }
            if (url == null) {
                url = WelcomeFrame.class.getClassLoader().getResource("resources/background.jpg");
            }
            if (url != null) {
                BufferedImage img = ImageIO.read(url);
                if (img != null) return img;
            }
        } catch (Exception ignored) {}

        // 2. Try relative and absolute file paths
        String[] paths = {
            "resources/background.jpg",
            "src/resources/background.jpg",
            "out/production/A-partment-Helper/resources/background.jpg",
            "C:/Users/MYPC/Downloads/A-partment-Helper/resources/background.jpg",
            "C:/Users/MYPC/.gemini/antigravity/brain/1df6086e-6f7c-42f6-b23b-372dc6f9fae9/.user_uploaded/media_1793679541953_4584c3ef.jpg"
        };
        for (String p : paths) {
            try {
                File f = new File(p);
                if (f.exists()) {
                    BufferedImage img = ImageIO.read(f);
                    if (img != null) return img;
                }
            } catch (Exception ignored) {}
        }

        return null;
    }

    private static class BackgroundPanel extends JPanel {
        private final BufferedImage bgImage;

        public BackgroundPanel(BufferedImage img) {
            super(null); // absolute layout
            this.bgImage = img;
            setBackground(COFFEE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bgImage != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                g2.dispose();
            }
        }
    }
}
