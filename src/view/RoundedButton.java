package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RoundedButton extends JButton {
    private int cornerRadius = 24;
    private Color normalBg;
    private Color hoverBg;
    private Color pressedBg;
    private boolean isHovered = false;
    private boolean isPressed = false;

    public RoundedButton(String text, Color bgColor, Color fgColor, int cornerRadius) {
        super(text);
        this.cornerRadius = cornerRadius;
        this.normalBg = bgColor;
        this.hoverBg = brighten(bgColor, 0.12f);
        this.pressedBg = darken(bgColor, 0.15f);

        setForeground(fgColor);
        setFont(new Font("Segoe UI", Font.BOLD, 14));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    public RoundedButton(String text, int cornerRadius) {
        this(text, new Color(190, 124, 58), Color.WHITE, cornerRadius);
    }

    public void setNormalBackground(Color c) {
        this.normalBg = c;
        this.hoverBg = brighten(c, 0.12f);
        this.pressedBg = darken(c, 0.15f);
        repaint();
    }

    public int getCornerRadius() {
        return cornerRadius;
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    private static Color brighten(Color c, float factor) {
        int r = Math.min(255, (int) (c.getRed() + (255 - c.getRed()) * factor));
        int g = Math.min(255, (int) (c.getGreen() + (255 - c.getGreen()) * factor));
        int b = Math.min(255, (int) (c.getBlue() + (255 - c.getBlue()) * factor));
        return new Color(r, g, b, c.getAlpha());
    }

    private static Color darken(Color c, float factor) {
        int r = Math.max(0, (int) (c.getRed() * (1 - factor)));
        int g = Math.max(0, (int) (c.getGreen() * (1 - factor)));
        int b = Math.max(0, (int) (c.getBlue() * (1 - factor)));
        return new Color(r, g, b, c.getAlpha());
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color fill = normalBg;
        if (isPressed) {
            fill = pressedBg;
        } else if (isHovered) {
            fill = hoverBg;
        }

        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        // Subtle highlight line on border for a crisp modern look
        g2.setColor(new Color(255, 255, 255, 40));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        g2.dispose();

        super.paintComponent(g);
    }
}

