package util;

import java.security.SecureRandom;

public class CodeGenerator {

    private static final String CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static String generateCode() {

        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < 8; i++) {

            if (i == 4)
                code.append("-");

            code.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }

        return code.toString();
    }
}