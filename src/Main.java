import view.WelcomeFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Enable sharp font anti-aliasing across Windows and Linux
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Start the application safely on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> new WelcomeFrame());
    }
}