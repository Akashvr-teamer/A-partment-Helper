import view.WelcomeFrame;

public class Main {
    public static void main(String[] args) {
        // Enable sharp font anti-aliasing across Windows and Linux
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Start the application by displaying the Welcome screen
        new WelcomeFrame();
    }
}