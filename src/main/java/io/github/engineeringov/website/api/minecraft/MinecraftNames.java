package io.github.engineeringov.website.api.minecraft;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minecraft names. When a real (Mojang) player has the same name as one of our accounts, ours
 * shows as {@code Name(1)} and the real ones as {@code Name(2)}, {@code Name(3)}... Real names
 * can't contain parentheses, so a suffixed name never belongs to anyone else.
 */
public final class MinecraftNames {
    public static final int MAX = 16;
    private static final Pattern VALID = Pattern.compile("[A-Za-z0-9_]{3,16}");
    private static final Pattern SUFFIXED = Pattern.compile("(.{1,15})\\((\\d{1,3})\\)");

    private MinecraftNames() {
    }

    public static boolean valid(String name) {
        return name != null && VALID.matcher(name).matches();
    }

    /** {@code Bob} + 2 -> {@code Bob(2)}, cut so the result stays within 16 characters. */
    public static String withSuffix(String name, int n) {
        String s = "(" + n + ")";
        return name.substring(0, Math.min(name.length(), MAX - s.length())) + s;
    }

    /** Case-insensitive key, as Minecraft compares names. */
    public static String key(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    /** {@code Bob(2)} -> ("Bob", 2), or null for a plain name. */
    public static Suffixed parse(String name) {
        if (name == null) return null;
        Matcher m = SUFFIXED.matcher(name);
        return m.matches() ? new Suffixed(m.group(1), Integer.parseInt(m.group(2))) : null;
    }

    /** {@code base} may be cut: {@code SixteenCharsN(1)} came from {@code SixteenCharsName}. */
    public record Suffixed(String base, int n) {
        /** True when {@code fullName} shows as this suffixed name. */
        public boolean is(String fullName) {
            return withSuffix(fullName, n).equalsIgnoreCase(withSuffix(base, n));
        }
    }
}
