package dev.execute.advancedjoin.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.builder()
                    .character('&')
                    .hexColors()
                    .useUnusualXRepeatedCharacterHexFormat()
                    .build();

    private ColorUtil() {}

    public static Component parse(String text) {
        if (text == null) return Component.empty();
        return LEGACY.deserialize(convertHexToLegacy(text))
                .decoration(TextDecoration.ITALIC, false);
    }

    public static String replace(String text, String... replacements) {
        if (text == null) return "";
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            text = text.replace(replacements[i], replacements[i + 1]);
        }
        return text;
    }

    private static String convertHexToLegacy(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder r = new StringBuilder("&x");
            for (char c : hex.toCharArray()) r.append('&').append(c);
            matcher.appendReplacement(sb, r.toString());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
