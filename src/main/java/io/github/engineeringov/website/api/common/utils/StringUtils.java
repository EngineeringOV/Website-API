package io.github.engineeringov.website.api.common.utils;

public class StringUtils {
    private StringUtils() {
    }

    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    public static String buildResetAccountUrl(String email, String uuid, String resetUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append(resetUrl).append("?email=")
                .append(email)
                .append("&uuid=")
                .append(uuid);
        return sb.toString();
    }

    public static String cleanWhiteSpaces(String parseable) {
        if (parseable == null) {
            return null;
        }
        return parseable.replaceAll("\\s+", " ");
    }
}
