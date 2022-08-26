package ventures.of.api.common.utils;

import java.util.Arrays;

public class StringUtils {
    private StringUtils() {
    }

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static String nullSafeString(String str) {
        return isEmpty(str) ? str : "";
    }

    public static String[] nullSafeString(String... str) {
        return (String[]) Arrays.stream(str).map(StringUtils::nullSafeString).toArray();
    }

    public static boolean doesNotContainEmptyOrNull(String... strings) {
        return Arrays.stream(strings).noneMatch(StringUtils::isEmpty);
    }

    public static String matchStringLength(String length, String expectedLength) {
        if (length.length() == expectedLength.length()) {
            return length;
        } else if (length.length() > expectedLength.length()) {
            return length.substring(0, expectedLength.length());
        } else {
            StringBuilder retStr = new StringBuilder(length);
            for (int i = retStr.length(); i < expectedLength.length(); i++) {
                retStr.append(" ");
            }
            return retStr.toString();
        }
    }

    public static void printOnMatch(String name, String value, String expected) {
        boolean hash2Match = (value.equalsIgnoreCase(expected));
        String nameEquals = name + " = ";
        System.out.println((hash2Match ? ANSI_GREEN : ANSI_RED) + nameEquals + value);
        if (!hash2Match) {
            System.out.println(matchStringLength("vs =", nameEquals) + expected + "\n");
        }
        System.out.print(ANSI_RESET);
    }

    public static String buildResetAccountUrl(String email, String uuid) {
        StringBuilder sb = new StringBuilder();
        sb.append("https://world.of.ventures/confirmPasswordChange.html?email=")
                .append(email)
                .append("&uuid=")
                .append(uuid);
        return sb.toString();
    }

}
