package util;

import java.security.MessageDigest;
import java.security.SecureRandom;

public class CodeGenerator {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    // Generates a 4-character pass code (e.g. K7M2)
    public static String generateCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            code.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }

        return code.toString();
    }

    // Generates a 3-character exit code (e.g. 7B2)
    public static String generateExitCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < 3; i++) {
            code.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }

        return code.toString();
    }

    // Hashes the access code using SHA-256 for secure DB lookup
    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.trim().toUpperCase().getBytes());
            return java.util.HexFormat.of().formatHex(bytes);
        } catch (Exception e) {
            return "";
        }
    }
}