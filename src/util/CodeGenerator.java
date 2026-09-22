package util;

import java.security.MessageDigest;
import java.security.SecureRandom;

public class CodeGenerator {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    // Generates an 8-character pass code formatted as XXXX-XXXX
    public static String generateCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            if (i == 4) code.append("-");
            code.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }

        return code.toString();
    }

    // Hashes the access code using SHA-256 for secure DB lookup
    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.trim().toUpperCase().getBytes());
            StringBuilder sb = new StringBuilder();

            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}