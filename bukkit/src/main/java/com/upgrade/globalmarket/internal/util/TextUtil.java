package com.upgrade.globalmarket.internal.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Blank038
 */
public class TextUtil {
    private static final Pattern PATTERN = Pattern.compile("#[A-f0-9]{6}");
    private static boolean USE_BUNGEE_CHAT = false;

    static {
        try {
            // Try to load BungeeCord ChatColor class (may be shaded)
            Class.forName("net.md_5.bungee.api.ChatColor");
            USE_BUNGEE_CHAT = true;
        } catch (ClassNotFoundException e) {
            try {
                // Try shaded version
                Class.forName("com.upgrade.globalmarket.libs.bungee.api.ChatColor");
                USE_BUNGEE_CHAT = true;
            } catch (ClassNotFoundException e2) {
                USE_BUNGEE_CHAT = false;
            }
        }
    }

    public static String formatHexColor(String message) {
        if (message == null) {
            return null;
        }
        String copy = message;
        Matcher matcher = PATTERN.matcher(copy);
        while (matcher.find()) {
            String color = message.substring(matcher.start(), matcher.end());
            try {
                copy = copy.replace(color, formatHexColorInternal(color));
            } catch (Exception e) {
                // Fallback: just remove the hex color code
                copy = copy.replace(color, "");
            }
        }
        return translateColorCodes('&', copy);
    }

    private static String formatHexColorInternal(String hexColor) throws Exception {
        if (USE_BUNGEE_CHAT) {
            try {
                // Try standard BungeeCord path
                Class<?> chatColorClass = Class.forName("net.md_5.bungee.api.ChatColor");
                Object colorObj = chatColorClass.getMethod("of", String.class).invoke(null, hexColor);
                return colorObj.toString();
            } catch (Exception e) {
                // Try shaded path
                Class<?> chatColorClass = Class.forName("com.upgrade.globalmarket.libs.bungee.api.ChatColor");
                Object colorObj = chatColorClass.getMethod("of", String.class).invoke(null, hexColor);
                return colorObj.toString();
            }
        }
        // Fallback: return empty string
        return "";
    }

    private static String translateColorCodes(char altChar, String text) {
        if (USE_BUNGEE_CHAT) {
            try {
                try {
                    Class<?> chatColorClass = Class.forName("net.md_5.bungee.api.ChatColor");
                    Object result = chatColorClass.getMethod("translateAlternateColorCodes", char.class, String.class)
                            .invoke(null, altChar, text);
                    return result.toString();
                } catch (Exception e) {
                    Class<?> chatColorClass = Class.forName("com.upgrade.globalmarket.libs.bungee.api.ChatColor");
                    Object result = chatColorClass.getMethod("translateAlternateColorCodes", char.class, String.class)
                            .invoke(null, altChar, text);
                    return result.toString();
                }
            } catch (Exception e) {
                // Fallback to simple replacement
                return translateColorCodesFallback(altChar, text);
            }
        }
        return translateColorCodesFallback(altChar, text);
    }

    private static String translateColorCodesFallback(char altChar, String text) {
        char[] b = text.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1) {
                b[i] = '\u00A7';
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }
}

